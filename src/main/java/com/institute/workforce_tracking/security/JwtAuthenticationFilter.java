package com.institute.workforce_tracking.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Authenticates requests that carry a valid JWT.
 *
 * <p>Runs once per request. If the {@code Authorization: Bearer <token>} header
 * holds a valid token, this filter loads the corresponding user and places an
 * authenticated token into the {@link SecurityContextHolder}, so downstream
 * authorization rules and controllers see the request as authenticated. If no
 * (or an invalid) token is present, the request simply continues
 * unauthenticated — protected endpoints then reject it via the entry point.</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String token = extractToken(request);

        try {
            if (token != null
                    && jwtUtil.isTokenValid(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                final String email = jwtUtil.extractEmail(token);
                final UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Never let a token problem crash the request; just leave it
            // unauthenticated so protected routes return a clean 401.
            log.warn("Could not authenticate request from JWT: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the raw JWT from the Authorization header, or null if absent
     * or not a Bearer token.
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}