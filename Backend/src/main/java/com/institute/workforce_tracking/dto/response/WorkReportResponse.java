package com.institute.workforce_tracking.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Outbound DTO for a work report.
 *
 * @param id              report primary key
 * @param userId          the user's id
 * @param userFullName    the user's full name
 * @param workDate        the day the report covers
 * @param reportText      what was accomplished
 * @param submittedAt     when the report was submitted
 * @param checkInTime     the attendance first login (check-in) time for the day
 * @param checkoutTime    the attendance checkout time (deadline = this + 24h)
 * @param plannedStartTime the declared work-schedule start (null if no plan)
 * @param plannedEndTime  the declared work-schedule end (null if no plan)
 * @param plannedWork     the declared work description (null if no plan)
 * @param submittedLate   whether submitted after the 24h window
 */
public record WorkReportResponse(
        Long id,
        Long userId,
        String userFullName,
        LocalDate workDate,
        String reportText,
        LocalDateTime submittedAt,
        LocalDateTime checkInTime,
        LocalDateTime checkoutTime,
        LocalTime plannedStartTime,
        LocalTime plannedEndTime,
        String plannedWork,
        boolean submittedLate
) {
}
