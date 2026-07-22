package com.institute.workforce_tracking.event;

import java.time.LocalTime;

/**
 * Domain event published shortly before a user's declared work-start time, so
 * they get a heads-up to check in on time. Delivered in-app, by web push, and
 * by email.
 *
 * @param userId        the user's id
 * @param email         the user's email (WebSocket principal + mail address)
 * @param fullName      the user's display name (for the email greeting)
 * @param plannedStart  the start time the user declared in their work plan
 */
public record WorkStartReminderEvent(
        Long userId,
        String email,
        String fullName,
        LocalTime plannedStart
) {
}
