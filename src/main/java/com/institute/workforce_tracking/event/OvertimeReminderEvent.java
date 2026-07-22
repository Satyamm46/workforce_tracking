package com.institute.workforce_tracking.event;

import java.time.LocalDateTime;

/**
 * Domain event published when a working employee's overtime window is about to
 * close (a few minutes before the deadline), prompting them to extend or check
 * out. Delivered in-app, by web push, and by email.
 *
 * @param userId    the employee's id
 * @param email     the employee's email (WebSocket principal + mail address)
 * @param fullName  the employee's display name (for the email greeting)
 * @param deadline  when the current overtime window closes (auto-checkout)
 */
public record OvertimeReminderEvent(
        Long userId,
        String email,
        String fullName,
        LocalDateTime deadline
) {
}
