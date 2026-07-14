package com.institute.workforce_tracking.dto.response;

import java.time.Instant;
import java.time.LocalDate;

import com.institute.workforce_tracking.enums.LeaveStatus;

/**
 * Outbound representation of a leave request.
 *
 * @param id                the request id
 * @param userId            the requesting employee's id
 * @param userFullName      the requesting employee's name (for admin views)
 * @param startDate         first day of leave (inclusive)
 * @param endDate           last day of leave (inclusive)
 * @param totalDays         number of days requested (derived)
 * @param reason            the employee's reason
 * @param status            workflow state
 * @param appliedAt         when the request was submitted
 * @param decidedByFullName the deciding admin's name; null while pending
 * @param decisionComment   the admin's note; null if none
 */
public record LeaveResponse(
        Long id,
        Long userId,
        String userFullName,
        LocalDate startDate,
        LocalDate endDate,
        long totalDays,
        String reason,
        LeaveStatus status,
        Instant appliedAt,
        String decidedByFullName,
        String decisionComment
) {
}
