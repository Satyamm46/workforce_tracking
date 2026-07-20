package com.institute.workforce_tracking.service;

import com.institute.workforce_tracking.dto.request.PushSubscriptionRequest;

/**
 * Web Push (browser/phone) notification delivery and subscription management.
 *
 * <p>Sending is a courtesy channel: implementations must be fail-safe and do
 * nothing when VAPID keys are not configured.</p>
 */
public interface PushService {

    /** The VAPID public key browsers need to subscribe; empty if unconfigured. */
    String getPublicKey();

    /** Registers (or refreshes) a browser subscription for the user. */
    void subscribe(String email, PushSubscriptionRequest request);

    /** Removes a browser subscription by its endpoint. */
    void unsubscribe(String email, String endpoint);

    /**
     * Sends a push notification to every browser the user has subscribed.
     * Best-effort — never throws; dead subscriptions are pruned.
     */
    void sendToUser(Long userId, String title, String body);
}
