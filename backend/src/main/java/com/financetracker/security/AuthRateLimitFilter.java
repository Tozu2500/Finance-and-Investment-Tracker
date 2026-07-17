package com.financetracker.security;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * In-memory FIXED-window rate limiter for the credential endpoints.
 * Blunts brute-force password guessing without any external dependency.
 *
 * Known precision limit of fixed windows: a burst straddling a window
 * boundary can achieve up to (2×limit − 1) requests across ~2 windows.
 * Acceptable for blunting online guessing; the Bucket4j/Redis upgrade
 * (roadmap §2.11–2.12) replaces this with true rolling limits and
 * multi-node state when the app runs on more than one backend.
 */

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)  // after GlobalCorsFilter and before security chain
@Slf4j
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000;

    @Value("${app.rate-limit.auth-requests-per-minute:15}")
    private int maxRequestsPerWindow;

    private record Window(long startMs, AtomicInteger count) {}

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String uri = request.getRequestURI();
        // /me/password is authenticated but accepts currentPassword, so a
        // stolen token must not become an offline-speed oracle for guessing
        // the account password (guess it → change it → full takeover).
        return !(uri.startsWith("/api/auth/login")
                || uri.startsWith("/api/auth/register")
                || uri.startsWith("/api/auth/me/password"))
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
                throws ServletException, IOException {
        
        long now = System.currentTimeMillis();
        String clientKey = clientKey(request);

        Window w = windows.compute(clientKey, (k, existing) ->
                (existing == null || now - existing.startMs() >= WINDOW_MS)
                ? new Window(now, new AtomicInteger(0))
                : existing);

        if (w.count().incrementAndGet() > maxRequestsPerWindow) {
            long retryAfterSec = Math.max(1, (w.startMs() + WINDOW_MS - now) / 1000);
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(retryAfterSec));
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"message\":\"Too many attempts — try again in "
                        + retryAfterSec + " seconds\"}");
            log.warn("Rate limit hit for {} on {}", clientKey, request.getRequestURI());
            
            return;
        }

        if (windows.size() > 10_000) pruneExpired(now);

        filterChain.doFilter(request, response);
    }

    /**
     * Uses the direct socket address, not X-Forwarded-For — the header is
     * client-controlled and would let an attacker rotate identities. If you
     * deploy behind a trusted reverse proxy, configure
     * server.forwarded-headers-strategy=framework instead so getRemoteAddr()
     * is resolved by the framework from the trusted proxy chain.
     *
     * Loopback and IPv4-mapped forms are normalized so one local client
     * can't triple its budget by alternating 127.0.0.1 / ::1 / ::ffff:...
     * (Windows dual-stack resolves localhost to either family).
     */

    private String clientKey(HttpServletRequest request) {
        String addr = request.getRemoteAddr();

        if (addr == null) return "unknown";
        if ("::1".equals(addr) || "0:0:0:0:0:0:0:1".equals(addr)) return "127.0.0.1";

        String lower = addr.toLowerCase(Locale.ROOT);
        if (lower.startsWith("::ffff:")) return lower.substring(7);

        return lower;
    }

    private void pruneExpired(long now) {
        Iterator<Map.Entry<String, Window>> it = windows.entrySet().iterator();

        while (it.hasNext()) {
            if (now - it.next().getValue().startMs() >= WINDOW_MS) it.remove();
        }

        // Hard ceiling: under a distributed flood of distinct source addresses
        // even live windows could exhaust memory. Dropping all counters resets
        // rate budgets for one window — an acceptable trade against OOM.

        if (windows.size() > 50_000) {
            log.error("Rate-limit table has exceeded 50k live entries, clearing (possible distributed flood)");
            windows.clear();
        }
    }
}
