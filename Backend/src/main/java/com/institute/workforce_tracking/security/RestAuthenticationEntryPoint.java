package com.institute.workforce_tracking.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.dto.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Entry point invoked when an unauthenticated request hits a protected
 * resource.
 *
 * <p>By default Spring Security answers such requests with an HTML login page
 * or a browser Basic-Auth popup. For a JSON REST API that is wrong — clients
 * expect JSON. This component overrides that behaviour and writes our standard
 * {@link ErrorResponse} with HTTP 401, so <em>every</em> failure in the system
 * (authorization included) shares one shape.</p>
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /** Minimal, dependency-free payload used only if serialization ever fails. */
    private static final String FALLBACK_JSON =
            "{\"success\":false,\"message\":\"Authentication required\","
                    + "\"error\":\"UNAUTHORIZED\",\"status\":401}";

    private final ObjectMapper objectMapper;

    /**
     * @param objectMapper the Spring-managed Jackson mapper (Jackson 3),
     *                     injected so serialization matches the rest of the app
     */
    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorResponse body = ErrorResponse.of(
                "Authentication is required to access this resource.",
                "UNAUTHORIZED",
                HttpStatus.UNAUTHORIZED.value(),
                request.getRequestURI());

        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (Exception ex) {
            // The entry point must never itself throw while reporting an error,
            // so we fall back to a fixed, valid JSON payload as a safety net.
            json = FALLBACK_JSON;
        }

        response.getWriter().write(json);
    }
}