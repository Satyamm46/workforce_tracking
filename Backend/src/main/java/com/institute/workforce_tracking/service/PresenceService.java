package com.institute.workforce_tracking.service;

/**
 * Answers "is this user's browser currently connected?".
 *
 * <p>Defined in the service layer so attendance logic can depend on presence
 * without knowing it is implemented with WebSocket session tracking.</p>
 */
public interface PresenceService {

    /**
     * Whether the user has had no active connection for at least the given
     * grace period. The grace period absorbs page refreshes and short network
     * blips (the frontend STOMP client auto-reconnects every 5 seconds).
     *
     * @param email        the user's login email (the WebSocket principal name)
     * @param graceSeconds how long they must have been disconnected
     * @return true if the user has been offline for the full grace period
     */
    boolean isOffline(String email, long graceSeconds);
}
