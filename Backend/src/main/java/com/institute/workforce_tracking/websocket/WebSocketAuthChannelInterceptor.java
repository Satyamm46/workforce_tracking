package com.institute.workforce_tracking.websocket;

import java.util.List;
import java.util.Set;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.security.JwtUtil;

/**
 * WebSocket security in two stages: CONNECT frames are authenticated with the
 * same JWT that guards the REST API, and SUBSCRIBE frames to protected topics
 * are authorized against the session's role.
 */
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /** Destinations only management roles may subscribe to. */
    private static final String DASHBOARD_TOPIC = "/topic/dashboard";
    private static final Set<String> MANAGER_AUTHORITIES =
            Set.of("ROLE_SUPER_ADMIN", "ROLE_ADMIN");

    private final JwtUtil jwtUtil;

    public WebSocketAuthChannelInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            authorizeSubscription(accessor);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
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
                email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(DASHBOARD_TOPIC)) {
            return; // only the dashboard topic is restricted
        }

        boolean isManager = accessor.getUser() instanceof Authentication auth
                && auth.getAuthorities().stream()
                        .anyMatch(a -> MANAGER_AUTHORITIES.contains(a.getAuthority()));

        if (!isManager) {
            throw new AccessDeniedException("Not authorized to subscribe to " + destination);
        }
    }
}
