package com.financetracker.security;

import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {

    /**
     * Stamped into every issued token and REQUIRED back at validation.
     * A token signed with the same secret but minted by any other
     * application (or an older build of this one) is rejected, which
     * shrinks the blast radius of an accidentally shared/reused secret.
     */
    private static final String ISSUER = "finance-suite";
    private static final String AUDIENCE = "finance-suite-api";

    /** Tolerated clock drift between token issuer and validator. */
    private static final long CLOCK_SKEW_SECONDS = 30;

    /** Slack added to the configured lifetime when sanity-checking exp-iat. */
    private static final long LIFETIME_SLACK_MS = 60_000;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    /** The dev fallback secret committed in application.properties. */
    private static final String DEFAULT_DEV_SECRET =
            "ZmluYW5jZS10cmFja2VyLXNlY3JldC1rZXktMjAyNi1zdXBlci1zZWN1cmUtcGxlYXNlLWNoYW5nZS1tZQ==";

    private SecretKey signingKey;
    private JwtParser parser;

    @PostConstruct
    void init() {
        byte[] decoded = Decoders.BASE64.decode(jwtSecret);
        if (decoded.length < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret must decode to at least 32 bytes for HMAC-SHA256");
        }
        if (DEFAULT_DEV_SECRET.equals(jwtSecret)) {
            log.warn("Using the built-in DEVELOPMENT JWT secret — set the JWT_SECRET "
                    + "environment variable before exposing this server to a network.");
        }
        signingKey = Keys.hmacShaKeyFor(decoded);
        // Built once: the parser is thread-safe and enforces signature,
        // expiry (with bounded skew), issuer and audience on every parse —
        // there is no code path that reads claims from an unverified token.
        parser = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(ISSUER)
                .requireAudience(AUDIENCE)
                .clockSkewSeconds(CLOCK_SKEW_SECONDS)
                .build();
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())   // jti: unique per token, enables future revocation lists
                .subject(userDetails.getUsername())
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtExpiration))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = parser.parseSignedClaims(token).getPayload();
            String subject = claims.getSubject();
            if (subject == null || subject.isBlank() || !subject.equals(userDetails.getUsername())) {
                return false;
            }
            // A well-formed token from this server always carries iat/exp with
            // exp - iat == configured lifetime. Anything longer-lived than we
            // ever issue is forged or misconfigured — reject it even though
            // the signature verifies.
            Date issuedAt = claims.getIssuedAt();
            Date expiration = claims.getExpiration();
            if (issuedAt == null || expiration == null) {
                return false;
            }
            return expiration.getTime() - issuedAt.getTime() <= jwtExpiration + LIFETIME_SLACK_MS;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(parser.parseSignedClaims(token).getPayload());
    }
}
