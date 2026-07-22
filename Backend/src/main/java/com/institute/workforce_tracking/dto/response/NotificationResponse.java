package com.institute.workforce_tracking.dto.response;

import java.time.Instant;

import com.institute.workforce_tracking.enums.NotificationType;

/**
 * Outbound representation of a notification — used identically by the REST
 * history endpoints and the WebSocket push, so both channels speak one shape.
 *
 * @param id        the notification id
 * @param type      category (drives frontend icon/colour)
 * @param message   the display text
 * @param read      whether the user has seen it
 * @param createdAt when it was generated
 */
public record NotificationResponse(
        Long id,
        NotificationType type,
        String message,
        boolean read,
        Instant createdAt
) {
}
