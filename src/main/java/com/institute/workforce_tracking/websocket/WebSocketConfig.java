package com.institute.workforce_tracking.websocket;

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
        this.allowedOrigins = allowedOrigins.split(",");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The HTTP upgrade endpoint the client connects to (full path: /api/ws).
        registry.addEndpoint("/ws").setAllowedOrigins(allowedOrigins);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-memory broker delivering to subscription destinations.
        registry.enableSimpleBroker("/queue");
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
