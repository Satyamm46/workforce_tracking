package com.institute.workforce_tracking.event;

/**
 * Domain event published when the system acts on a user's attendance without
 * their input: an auto-started break (they disconnected) or an auto checkout
 * (their auto-break exceeded the limit). Listeners notify the affected user.
 *
 * @param userId     the affected user's id
 * @param email      the affected user's email
 * @param checkedOut false = placed on break; true = checked out
 */
public record AttendanceAutoActionEvent(
        Long userId,
        String email,
        boolean checkedOut
) {
}
