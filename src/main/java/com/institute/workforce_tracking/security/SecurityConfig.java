package com.institute.workforce_tracking.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration.
 *
 * <p>Configures a stateless, JWT-authenticated REST API: browser-oriented
 * defaults are disabled, public routes are declared, the custom JWT filter is
 * positioned in the chain, and unauthorized access returns a JSON 401.</p>
 *
 * <p>The WebSocket handshake endpoint is public because the browser WebSocket
 * API cannot send an Authorization header on the HTTP upgrade request;
 * WebSocket sessions are authenticated at the STOMP CONNECT frame instead
 * (see {@code WebSocketAuthChannelInterceptor}).</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Endpoints reachable without authentication. Paths are relative to the
     * servlet context (the {@code /api} context-path is stripped before
     * matching), so {@code "/v1/auth/login"} matches {@code /api/v1/auth/login}.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/health",
            "/actuator/health",
            "/actuator/info",
            "/v1/auth/login",
            "/ws/**"
    };

    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(RestAuthenticationEntryPoint authenticationEntryPoint,
                          JwtUtil jwtUtil,
                          UserDetailsService userDetailsService) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Exposes Spring's global AuthenticationManager as a bean so the
     * AuthService can perform programmatic authentication.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Defines the application's single security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // Public self-registration: only the submit action.
                        // Reviewing/deciding stays authenticated (Super Admin).
                        .requestMatchers(HttpMethod.POST, "/v1/registrations").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(authenticationEntryPoint))
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
