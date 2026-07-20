package com.institute.workforce_tracking.event;

import java.time.LocalTime;

/**
 * Domain event published when a user's first check-in came more than the
 * grace period after their planned start time, making the day a half day.
 *
 * @param userId       the late user's id
 * @param email        the late user's email
 * @param plannedStart the start time they declared in their work plan
 * @param minutesLate  how late the check-in was, in minutes
 */
public record LateArrivalEvent(
        Long userId,
        String email,
        LocalTime plannedStart,
        long minutesLate
) {
}
