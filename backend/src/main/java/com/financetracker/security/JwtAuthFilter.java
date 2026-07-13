package com.financetracker.security;

import java.io.IOException;
import java.util.Locale;

import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private static final String BEARER_PREFIX = "bearer ";

    /**
     * Our tokens are ~300 chars. Parsing is only attempted on plausibly-sized
     * headers so a multi-megabyte Authorization value can't burn CPU in
     * base64/JSON decoding before validation has a chance to reject it.
     */

    private static final int MAX_AUTH_HEADER_LENGTH = 2048;

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull FilterChain filterChain)
                throws ServletException, IOException {

        final String jwt = extractBearerToken(request);
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Fails closed: ANY parse/validation problem leaves the request
        // unauthenticated and the request proceeds to be rejected by the
        // authorization rules — this filter never sends a response itself.

        try {
            final String email = jwtTokenProvider.extractUsername(jwt);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (jwtTokenProvider.isTokenValid(jwt, userDetails)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException ex) {
            // Distinguishable from "invalid" so clients are able to silently
            // re-authenticate instead of showing an error state
            response.setHeader("X-Auth-Expired", "true");
            log.debug("Expired JWT on request to {}", request.getRequestURI());
        } catch (Exception ex) {
            log.debug("JWT authentication failed for request to {}: {}", request.getRequestURI(), ex.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Returns the token from a synthetically plausible Bearer header
     * (scheme matched per RFC 7235), or null when the header is
     * absent, malformed, oversized or not JWT-shaped.
     */
    private String extractBearerToken(HttpServletRequest request) {
        final String header = request.getHeader("Authorization");

        if (header == null || header.length() > MAX_AUTH_HEADER_LENGTH || !header.toLowerCase(Locale.ROOT).startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();

        // A JWS compact serialization has exactly three dot-separated parts.

        int firstDot = token.indexOf('.');
        int lastDot = token.lastIndexOf('.');

        if (token.isEmpty() || firstDot <= 0 || firstDot == lastDot || lastDot == token.length() - 1) {
            return null;
        }

        // Each part must be canonical unpadded base64url. In particular a
        // length of 1 (mod 4) is impossible in strict encoding, but lenient
        // decoders silently drop the dangling 6 bits — which makes
        // "<token>x" verify as the original signature. Rejecting the shape
        // here removes that token malleability.
        
        if (!isCanonicalBase64Url(token, 0, firstDot)
                || !isCanonicalBase64Url(token, firstDot + 1, lastDot)
                || !isCanonicalBase64Url(token, lastDot + 1, token.length())) {
            return null;
        }
        return token;
    }

    private static boolean isCanonicalBase64Url(String s, int from, int to) {

    }
}
