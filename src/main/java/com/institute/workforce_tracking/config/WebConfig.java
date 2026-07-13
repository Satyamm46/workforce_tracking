package com.institute.workforce_tracking.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Web-layer configuration — currently the single source of truth for CORS.
 *
 * <p>Exposes a {@link CorsConfigurationSource} bean rather than using the
 * {@code WebMvcConfigurer#addCorsMappings} shortcut. The reason is important:
 * Spring Security is on the classpath, and when we later enable
 * {@code http.cors(...)} in SecurityConfig, Spring Security automatically
 * discovers a {@code CorsConfigurationSource} bean and applies it inside the
 * security filter chain. Defining it as a bean here means MVC <em>and</em>
 * Security share ONE CORS policy — no duplication, no drift.</p>
 */
@Configuration
public class WebConfig {

    /** Origins allowed to call this API, read from configuration. */
    private final List<String> allowedOrigins;

    /**
     * Constructor injection of the comma-separated origins property.
     * We split it into a list here so the bean method stays clean.
     *
     * @param allowedOrigins comma-separated origins from
     *                       {@code app.cors.allowed-origins}
     */
    public WebConfig(@Value("${app.cors.allowed-origins}") String allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }

    /**
     * Builds the global CORS policy applied to every endpoint.
     *
     * @return a CORS source mapping the policy to all paths
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Which browser origins may call this API (e.g. the React dev server).
        config.setAllowedOrigins(allowedOrigins);

        // HTTP methods the frontend is permitted to use.
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Request headers the frontend may send (Authorization carries the JWT later).
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // Response headers the browser is allowed to read.
        config.setExposedHeaders(List.of("Authorization"));

        // Allow cookies / Authorization header to be sent with cross-origin requests.
        config.setAllowCredentials(true);

        // Cache the CORS pre-flight (OPTIONS) response for 1 hour to cut round-trips.
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}