package com.institute.workforce_tracking.websocket;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP-over-WebSocket configuration.
 *
 * <p>Exposes the handshake endpoint, enables the in-memory message broker for
 * per-user queues, and installs the JWT channel interceptor so every session
 * is authenticated at CONNECT.</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthChannelInterceptor authChannelInterceptor;
    private final String[] allowedOrigins;

    public WebSocketConfig(WebSocketAuthChannelInterceptor authChannelInterceptor,
                           @Value("${app.cors.allowed-origins}") String allowedOrigins) {
        this.authChannelInterceptor = authChannelInterceptor;
        // Trim and drop blanks, matching WebConfig's parsing of the same
        // property. Without trimming, "a, b" yields " b" and that origin
        // silently never matches.
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The HTTP upgrade endpoint the client connects to (full path: /api/ws).
        //
        // Patterns, not exact origins: app.cors.allowed-origins may contain a
        // wildcard entry for hosts that mint a new deployment URL per push
        // (e.g. Vercel). setAllowedOrigins compares literally, so a '*' entry
        // would never match and the handshake would fail with 403 while plain
        // REST calls succeeded. Must stay in step with WebConfig, which reads
        // the same property.
        registry.addEndpoint("/ws").setAllowedOriginPatterns(allowedOrigins);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-memory broker delivering to subscription destinations. Each call
        // replaces the previous registration, so this must be declared once.
        registry.enableSimpleBroker("/queue", "/topic");
        // convertAndSendToUser(email, "/queue/x") → /user/{email}/queue/x
        registry.setUserDestinationPrefix("/user");
        // Prefix for client→server messages (unused yet; standard convention).
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
    }
}
