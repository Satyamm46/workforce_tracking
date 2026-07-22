package com.institute.workforce_tracking.event;

import com.institute.workforce_tracking.enums.Role;

/**
 * Domain event published when a user successfully logs in.
 *
 * <p>Carries an immutable snapshot of the essential user facts rather than the
 * JPA entity itself, so any listener — synchronous or (in the future)
 * asynchronous — can consume it safely without touching the publisher's
 * persistence context.</p>
 *
 * <p>Current listener: attendance (clock-in on first login of the day).
 * Future listeners can react to the same event — notifications, live
 * dashboard — with no change to the publisher.</p>
 *
 * @param userId   the authenticated user's id
 * @param email    the user's email
 * @param fullName the user's display name
 * @param role     the user's role
 */
public record UserLoggedInEvent(
        Long userId,
        String email,
        String fullName,
        Role role
) {
}