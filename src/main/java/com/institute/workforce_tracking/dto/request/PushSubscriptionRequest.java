package com.institute.workforce_tracking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for registering (or removing) a browser push subscription —
 * the JSON produced by {@code PushSubscription.toJSON()} in the browser.
 *
 * @param endpoint the push-service URL for this browser
 * @param p256dh   the client public key
 * @param auth     the client auth secret
 */
public record PushSubscriptionRequest(

        @NotBlank(message = "Endpoint is required")
        @Size(max = 512, message = "Endpoint must not exceed 512 characters")
        String endpoint,

        @NotBlank(message = "p256dh key is required")
        String p256dh,

        @NotBlank(message = "auth secret is required")
        String auth
) {
}
