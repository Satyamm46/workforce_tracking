package com.institute.workforce_tracking.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Outbound DTO for a work report.
 *
 * @param id           report primary key
 * @param userId       the user's id
 * @param userFullName the user's full name
 * @param workDate     the day the report covers
 * @param reportText   what was accomplished
 * @param submittedAt  when the report was submitted
 * @param checkoutTime the attendance checkout time (deadline = this + 24h)
 * @param submittedLate whether submitted after the 24h window
 */
public record WorkReportResponse(
        Long id,
        Long userId,
        String userFullName,
        LocalDate workDate,
        String reportText,
        LocalDateTime submittedAt,
        LocalDateTime checkoutTime,
        boolean submittedLate
) {
}
