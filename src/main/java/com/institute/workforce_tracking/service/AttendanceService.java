package com.institute.workforce_tracking.service;

import java.time.LocalDate;

import com.institute.workforce_tracking.dto.response.AttendanceResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;

/**
 * Business operations for attendance tracking.
 */
public interface AttendanceService {

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
}
