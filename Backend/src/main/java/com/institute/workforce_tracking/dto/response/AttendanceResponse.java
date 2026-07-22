package com.institute.workforce_tracking.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.institute.workforce_tracking.enums.AttendanceStatus;

/**
 * Outbound representation of one attendance record.
 *
 * <p>{@code workingMinutes} is always populated: while the day is in progress
 * it is a live "worked so far" snapshot; once the status is CHECKED_OUT it is
 * the final stored value. Clients read {@code status} to know which.</p>
 *
 * @param id                the record id
 * @param userId            the employee's id
 * @param userFullName      the employee's display name (for admin views)
 * @param workDate          the day this record covers
 * @param loginTime         first login of the day
 * @param logoutTime        clock-out time; null while still working
 * @param totalBreakMinutes accumulated break minutes (0 until breaks exist)
 * @param workingMinutes    minutes worked so far (final once checked out)
 * @param status            current state of the working day
 * @param lateArrival       checked in past the grace period after the planned start
 * @param halfDay           the day counts as a half day (late-arrival penalty)
 * @param absentNoReport    marked absent for not submitting a work report in time
 * @param overtimeDeadline  when the current overtime window closes; null if not in overtime
 */
public record AttendanceResponse(
        Long id,
        Long userId,
        String userFullName,
        LocalDate workDate,
        LocalDateTime loginTime,
        LocalDateTime logoutTime,
        int totalBreakMinutes,
        long workingMinutes,
        AttendanceStatus status,
        boolean lateArrival,
        boolean halfDay,
        boolean absentNoReport,
        LocalDateTime overtimeDeadline
) {
}