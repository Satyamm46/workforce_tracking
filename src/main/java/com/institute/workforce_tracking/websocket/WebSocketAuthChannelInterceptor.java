package com.institute.workforce_tracking.websocket;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.security.JwtUtil;

/**
 * Authenticates STOMP sessions at CONNECT time using the same JWT that guards
 * the REST API.
 *
 * <p>The browser WebSocket API cannot set an Authorization header on the HTTP
 * handshake, so the handshake endpoint is public and authentication happens
 * here instead: the client sends its token as a STOMP CONNECT header, this
 * interceptor validates it via {@link JwtUtil}, and the resulting principal is
 * attached to the session — which is what user-destination routing keys on.</p>
 */
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public WebSocketAuthChannelInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String header = accessor.getFirstNativeHeader(AUTH_HEADER);

            if (header == null || !header.startsWith(BEARER_PREFIX)) {
                throw new BadCredentialsException("Missing bearer token on STOMP CONNECT.");
            }
            String token = header.substring(BEARER_PREFIX.length());
            if (!jwtUtil.isTokenValid(token)) {
                throw new BadCredentialsException("Invalid or expired token on STOMP CONNECT.");
            }

            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);

            accessor.setUser(new UsernamePasswordAuthenticationToken(
                    email, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))));
        }

        return message;
    }
}
