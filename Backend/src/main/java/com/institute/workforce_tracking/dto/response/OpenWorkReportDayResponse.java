package com.institute.workforce_tracking.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A checked-out day the caller still owes a work report for, and can still
 * file one for.
 *
 * <p>Without this the older of two unreported days is invisible and
 * unreachable: submission targets the most recent checkout, so a deadline
 * extension granted for an earlier day buys time to fill in a form that
 * cannot be pointed at it.</p>
 *
 * @param workDate     the day the report would cover
 * @param checkoutTime when the user checked out that day
 * @param deadline     when the reporting window closes (checkout + 24h + any extension)
 * @param extraHours   hours added by an admin extension, 0 if none
 * @param markedAbsent whether the day is currently flagged absent for the missing report
 * @param overdue      whether the deadline has already passed, so the report files as late
 */
public record OpenWorkReportDayResponse(
        LocalDate workDate,
        LocalDateTime checkoutTime,
        LocalDateTime deadline,
        int extraHours,
        boolean markedAbsent,
        boolean overdue
) {
}
