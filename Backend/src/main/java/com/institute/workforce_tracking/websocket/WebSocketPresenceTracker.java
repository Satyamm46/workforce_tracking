package com.institute.workforce_tracking.websocket;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.institute.workforce_tracking.service.PresenceService;

/**
 * Tracks which users currently have an open WebSocket session.
 *
 * <p>The frontend keeps a STOMP connection for the whole authenticated
 * session (the notification stream), so "has an open WebSocket" is a good
 * proxy for "has the app open in a browser". The attendance auto-break sweep
 * uses this to detect users who disappeared without logging out.</p>
 *
 * <p>State is in-memory: after an application restart nobody is "present"
 * until their client reconnects (the frontend retries every 5 s), and unknown
 * users are only considered offline once the tracker itself has been running
 * longer than the caller's grace period — so a restart cannot mass-break
 * everyone instantly.</p>
 */
@Component
public class WebSocketPresenceTracker implements PresenceService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketPresenceTracker.class);

    /** Presence state for one user: open session count + last disconnect moment. */
    private record Presence(int activeSessions, Instant lastDisconnect) {
    }

    private final Map<String, Presence> presenceByEmail = new ConcurrentHashMap<>();

    /** When this tracker started observing — the floor for "offline since". */
    private final Instant startedAt = Instant.now();

    @EventListener
    public void onSessionConnected(SessionConnectedEvent event) {
        String email = principalName(event.getUser());
        if (email == null) {
            return;
        }
        presenceByEmail.merge(email, new Presence(1, null),
                (existing, ignored) -> new Presence(existing.activeSessions() + 1, null));
        log.debug("WebSocket connected: {}", email);
    }

    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        String email = principalName(event.getUser());
        if (email == null) {
            return;
        }
        presenceByEmail.compute(email, (key, existing) -> {
            int remaining = existing == null ? 0 : Math.max(existing.activeSessions() - 1, 0);
            return new Presence(remaining, remaining == 0 ? Instant.now() : null);
        });
        log.debug("WebSocket disconnected: {}", email);
    }

    @Override
    public boolean isOffline(String email, long graceSeconds) {
        Instant cutoff = Instant.now().minusSeconds(graceSeconds);
        Presence presence = presenceByEmail.get(email);

        if (presence == null) {
            // Never seen since startup: only offline once the tracker itself
            // has observed for a full grace period (protects app restarts).
            return startedAt.isBefore(cutoff);
        }
        if (presence.activeSessions() > 0) {
            return false;
        }
        return presence.lastDisconnect() != null && presence.lastDisconnect().isBefore(cutoff);
    }

    private String principalName(Principal principal) {
        return principal != null ? principal.getName() : null;
    }
}
