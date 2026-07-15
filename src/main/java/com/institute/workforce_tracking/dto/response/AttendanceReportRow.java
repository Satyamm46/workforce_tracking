package com.institute.workforce_tracking.dto.response;

/**
 * One employee's attendance summary for a reporting period.
 *
 * <p>Constructed DIRECTLY by a JPQL aggregate projection — the field order and
 * types must match the constructor expression in the repository query.
 * Aggregate fields are boxed {@code Long} because JPQL SUM/COUNT produce
 * {@code Long}.</p>
 *
 * @param userId         the employee's id
 * @param fullName       the employee's name
 * @param presentDays    days with a real attendance record (not leave)
 * @param leaveDays      days on approved leave
 * @param workingMinutes total minutes worked in the period
 * @param breakMinutes   total break minutes in the period
 */
public record AttendanceReportRow(
        Long userId,
        String fullName,
        Long presentDays,
        Long leaveDays,
        Long workingMinutes,
        Long breakMinutes
) {
}
