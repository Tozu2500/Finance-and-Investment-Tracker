package com.financetracker.config;


import com.financetracker.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.CrossOriginOpenerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.CrossOriginResourcePolicyHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Protocol and CORS setup
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)  // This'll be handled With GlobalCorsFilter

            // Request authorization
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )

            // Session strategy
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Exception handling: 401 on auth failure (not 403) so web clients can distinguish
            // from "forbidden" and auto-redirect to login
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) ->
                    writeJsonError(res, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required")
                )
                .accessDeniedHandler((req, res, ex) ->
                    writeJsonError(res, HttpServletResponse.SC_FORBIDDEN, "Access denied")
                )
            )

            // Security headers
            .headers(headers -> headers
                // Clickjacking protection
                .frameOptions(f -> f.deny())

                // MIME type sniffing protection
                .contentTypeOptions(o -> {})

                // XSS protection (legacy... CSP below is the primary)
                .xssProtection(x -> x.headerValue(XXssProtectionHeaderWriter.HeaderValue.DISABLED))

                // Referrer control
                .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))

                // CSP
                .contentSecurityPolicy(csp ->
                    csp.policyDirectives("default-src 'none'; frame-ancestors 'none'")
                )

                // Cross-origin opener policy
                .crossOriginOpenerPolicy(coop ->
                    coop.policy(CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy.SAME_ORIGIN)
                )

                // Cross-origin resource policy -- Block no cors embedding from foreign origins
                .crossOriginResourcePolicy(corp ->
                    corp.policy(CrossOriginResourcePolicyHeaderWriter.CrossOriginResourcePolicy.SAME_ORIGIN)
                )

                // Permissions policy - disable features not required at the moment for safety purposes
                .permissionsPolicy(p -> p.policy("geolocation=(), microphone=(), camera=()"))
            )

            // Auth & Filters
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static void writeJsonError(HttpServletResponse res, int status, String message)
                throws IOException {
        if (res.isCommitted()) return;
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"status\":" + status + ",\"message\":\"" + message + "\"}");
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Work factor 12 (~4x the v1 default of 10). Existing hashes keep
        // verifying — the cost factor is embedded per-hash — and are upgraded
        // to 12 whenever the password is next set or changed.
        return new BCryptPasswordEncoder(12);
    }

}
