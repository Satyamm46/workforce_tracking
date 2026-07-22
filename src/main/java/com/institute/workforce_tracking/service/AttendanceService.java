package com.institute.workforce_tracking.service;

import java.time.LocalDate;

import com.institute.workforce_tracking.dto.response.AttendanceResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;

/**
 * Business operations for attendance tracking.
 */
public interface AttendanceService {

    /**
     * Starts (or restarts) the caller's working day.
     *
     * <p>With no record today it clocks in fresh. After a clock-out it reopens
     * the day: the time away is recorded as a completed break, and work
     * continues on the same record. Invalid while already WORKING/ON_BREAK.</p>
     */
    AttendanceResponse checkIn(String email);

    /**
     * Ends the caller's working day. Any open break is auto-closed first, so
     * final working minutes account for it.
     */
    AttendanceResponse clockOut(String email);

    /**
     * Starts a break. Only valid while WORKING.
     */
    AttendanceResponse startBreak(String email);

    /**
     * Ends the current break and resumes work. Only valid while ON_BREAK.
     */
    AttendanceResponse resumeWork(String email);

    /** Returns the caller's attendance record for today. */
    AttendanceResponse getMyTodayAttendance(String email);

    /** Returns a page of the caller's attendance history, newest day first. */
    PagedResponse<AttendanceResponse> getMyAttendanceHistory(String email, int page, int size);

    /** Returns a page of ALL users' attendance for the given day (admin view). */
    PagedResponse<AttendanceResponse> getAttendanceByDate(LocalDate date, int page, int size);

        /**
     * Creates ON_LEAVE attendance records for each day in the range that has
     * no record yet. Called by the leave module when a request is approved;
     * participates in the caller's transaction.
     */
    void markLeaveDays(com.institute.workforce_tracking.entity.User user,
                       java.time.LocalDate startDate, java.time.LocalDate endDate);

    /**
     * Sweep: places every WORKING user whose browser has been disconnected
     * beyond the grace period on a system-started break. Called by the
     * attendance scheduler.
     *
     * @param graceSeconds how long a user must be offline before acting
     * @return how many users were placed on a break
     */
    int autoBreakAbsentUsers(long graceSeconds);

    /**
     * Sweep: checks out every user whose system-started break has exceeded
     * the limit. Manual breaks are never affected. Called by the scheduler.
     *
     * @param maxBreakMinutes the auto-break duration limit
     * @return how many users were checked out
     */
    int autoCheckoutOverdueBreaks(long maxBreakMinutes);

    /**
     * Sweep: publishes a reminder to each user whose declared work-start time
     * is approaching (within the reminder window) and who has not yet checked
     * in. Fires at most once per plan. Called by the workday scheduler.
     *
     * @return how many reminders were published
     */
    int publishStartReminders();

    /**
     * Sweep: manages overtime for WORKING employees past their declared logout
     * time — opens a 30-minute window, warns a few minutes before it closes,
     * and auto-checks-out anyone whose window closes without an extension.
     * Called by the workday scheduler.
     *
     * @return how many overtime actions were taken (reminders + checkouts)
     */
    int processOvertimeWindows();

    /**
     * Extends the caller's current overtime window by another block. Valid
     * only while WORKING and already in overtime.
     */
    AttendanceResponse extendOvertime(String email);

}
