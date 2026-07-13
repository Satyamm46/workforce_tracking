package com.institute.workforce_tracking.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration for the application.
 *
 * <p>This class defines the single security filter chain: it disables the
 * stateful, browser-oriented defaults (form login, HTTP Basic, CSRF, sessions)
 * and configures the application as a <strong>stateless JSON API</strong>
 * prepared for JWT authentication.</p>
 *
 * <p><strong>Foundation scope:</strong> the JWT filter is wired into the chain
 * as a pass-through and public routes are declared. No credentials are issued
 * or validated yet — that behaviour arrives with the authentication module,
 * without changing this chain's structure.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Endpoints reachable without authentication. Paths are relative to the
     * servlet context (context-path {@code /api} is stripped before matching),
     * so {@code "/health"} here matches the public URL {@code /api/health}.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/health",
            "/actuator/health",
            "/actuator/info"
    };

    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    /**
     * @param authenticationEntryPoint component that renders 401 responses as
     *                                 our standard JSON error envelope
     */
    public SecurityConfig(RestAuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    /**
     * Defines the application's single security filter chain.
     *
     * @param http the security builder provided by Spring
     * @return the configured filter chain
     * @throws Exception if the chain cannot be built
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Apply the shared CORS policy (discovers the CorsConfigurationSource bean).
                .cors(Customizer.withDefaults())

                // Stateless JWT API: CSRF tokens are unnecessary and are disabled.
                .csrf(csrf -> csrf.disable())

                // No server-side sessions — every request authenticates itself.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules: public routes open, everything else secured.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())

                // Return our JSON ErrorResponse (not an HTML page) on 401.
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(authenticationEntryPoint))

                // Disable the browser-oriented mechanisms we don't use.
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())

                // Position the (currently pass-through) JWT filter ahead of
                // Spring's username/password filter — the slot the real token
                // check will occupy once authentication is implemented.
                .addFilterBefore(new JwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}