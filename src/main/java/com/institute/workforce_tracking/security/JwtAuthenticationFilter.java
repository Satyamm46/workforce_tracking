package com.institute.workforce_tracking.security;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet filter placeholder for JWT-based authentication.
 *
 * <p>Extends {@link OncePerRequestFilter} so it runs exactly once per request.
 * In this foundation milestone it is a transparent pass-through: it forwards
 * every request down the chain unchanged.</p>
 *
 * <p>In the authentication milestone the {@code doFilterInternal} body will
 * read the {@code Authorization: Bearer <token>} header, validate the token
 * via {@link JwtUtil}, and populate the Spring {@code SecurityContext} for
 * authenticated requests. Its position in the filter chain (configured in
 * {@code SecurityConfig}) will not change — only this method's body will.</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Foundation milestone: no token processing yet — forward untouched.
        // Token extraction, validation, and SecurityContext population are
        // added here when the authentication module is implemented.
        filterChain.doFilter(request, response);
    }
}