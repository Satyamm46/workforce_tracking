package com.institute.workforce_tracking.event;

import java.time.LocalDate;

/**
 * Domain event published after a leave request is approved or rejected.
 *
 * @param userId    the requesting employee's id
 * @param email     the requesting employee's email
 * @param approved  true if approved, false if rejected
 * @param startDate first day of the requested leave
 * @param endDate   last day of the requested leave
 */
public record LeaveDecidedEvent(
        Long userId,
        String email,
        boolean approved,
        LocalDate startDate,
        LocalDate endDate
) {
}
