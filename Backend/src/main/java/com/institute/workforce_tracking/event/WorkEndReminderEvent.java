package com.institute.workforce_tracking.event;

import java.time.LocalTime;

/**
 * Domain event published shortly before a user's declared work-end time, so
 * they can decide to extend or wrap up before the overtime machinery takes
 * over. Delivered in-app, by web push, and by email.
 *
 * <p>The counterpart of {@link OvertimeReminderEvent}, which arrives only once
 * the user is already past their planned end and an auto-checkout is
 * pending. This one is the heads-up that gets in front of that.</p>
 *
 * @param userId     the user's id
 * @param email      the user's email (WebSocket principal + mail address)
 * @param fullName   the user's display name (for the email greeting)
 * @param plannedEnd the end time the user declared in their work plan
 */
public record WorkEndReminderEvent(
        Long userId,
        String email,
        String fullName,
        LocalTime plannedEnd
) {
}
