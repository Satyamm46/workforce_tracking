package com.institute.workforce_tracking.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/health",
            "/actuator/health",
            "/actuator/info",
            "/v1/auth/login"
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

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
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