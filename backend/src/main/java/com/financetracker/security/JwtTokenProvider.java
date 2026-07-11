package com.financetracker.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
public class JwtTokenProvider {

    // Stamped into every issued token and REQUIRED back at validation.
    // A token signed with the same secret but minted by any other
    // application (or an older build of this one) is rejected, which
    // shrinks the blast radius of an accidentally shared/reused secret.
    private static final String ISSUER = "finance-suite";
    private static final String AUDIENCE = "finance-suite-api";

    // Tolerated clock drift between token issuer and the validator
    private static final long CLOCK_SKEW_SECONDS = 30;

    // Slack added to configured lifetime when sanity-checking exp-iat
    private static final long LIFETIME_SLACK_MS = 60_000;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    // Dev fallback secret in application.properties
    private static final String DEFAULT_DEV_SECRET = "ZmluYW5jZS10cmFja2VyLXNlY3JldC1rZXktMjAyNi1zdXBlci1zZWN1cmUtcGxlYXNlLWNoYW5nZS1tZQ==";

    private SecretKey signingkey;
    private JwtParser parser;

    @PostConstruct
    void init() {
        byte[] decoded = Decoders.BASE64.decode(jwtSecret);
        if (decoded.length < 32) {
            throw new IllegalStateException("app.jwt.secret must decode at least 32 bytes for HMAC-SHA256");
        }

        if (DEFAULT_DEV_SECRET.equals(jwtSecret)) {
            log.warn("Using the built-in DEV JWT secret - set the JWT SECRET "
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


}
