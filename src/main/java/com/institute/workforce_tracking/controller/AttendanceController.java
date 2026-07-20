package com.institute.workforce_tracking.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.institute.workforce_tracking.constants.ApiConstants;
import com.institute.workforce_tracking.dto.response.AttendanceResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.dto.response.ApiResponse;
import com.institute.workforce_tracking.service.AttendanceService;
import com.institute.workforce_tracking.util.DateTimeUtil;

/**
 * REST endpoints for attendance tracking, including break management.
 */
@RestController
@RequestMapping(ApiConstants.ATTENDANCE_BASE)
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /** Starts or reopens the caller's working day. */
    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(Authentication authentication) {
        AttendanceResponse attendance = attendanceService.checkIn(authentication.getName());
        return ResponseEntity.ok(ApiResponse.of("Checked in successfully", attendance));
    }

    /** Ends the caller's working day (auto-closing any open break). */
    @PostMapping("/clock-out")
    public ResponseEntity<ApiResponse<AttendanceResponse>> clockOut(Authentication authentication) {
        AttendanceResponse attendance = attendanceService.clockOut(authentication.getName());
        return ResponseEntity.ok(ApiResponse.of("Clocked out successfully", attendance));
    }

    /** Starts a break for the caller. */
    @PostMapping("/break/start")
    public ResponseEntity<ApiResponse<AttendanceResponse>> startBreak(Authentication authentication) {
        AttendanceResponse attendance = attendanceService.startBreak(authentication.getName());
        return ResponseEntity.ok(ApiResponse.of("Break started", attendance));
    }

    /** Ends the caller's current break and resumes work. */
    @PostMapping("/break/end")
    public ResponseEntity<ApiResponse<AttendanceResponse>> resumeWork(Authentication authentication) {
        AttendanceResponse attendance = attendanceService.resumeWork(authentication.getName());
        return ResponseEntity.ok(ApiResponse.of("Work resumed", attendance));
    }

    /** Returns the caller's attendance record for today. */
    @GetMapping("/me/today")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getMyTodayAttendance(
            Authentication authentication) {
        AttendanceResponse attendance =
                attendanceService.getMyTodayAttendance(authentication.getName());
        return ResponseEntity.ok(ApiResponse.of("Today's attendance retrieved", attendance));
    }

    /** Returns a page of the caller's attendance history, newest first. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PagedResponse<AttendanceResponse>>> getMyAttendanceHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<AttendanceResponse> history =
                attendanceService.getMyAttendanceHistory(authentication.getName(), page, size);
        return ResponseEntity.ok(ApiResponse.of("Attendance history retrieved", history));
    }

    /** Admin view: all users' attendance for one day (defaults to today). */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<AttendanceResponse>>> getAttendanceByDate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LocalDate effectiveDate = (date != null) ? date : DateTimeUtil.today();
        PagedResponse<AttendanceResponse> attendance =
                attendanceService.getAttendanceByDate(effectiveDate, page, size);
        return ResponseEntity.ok(ApiResponse.of("Attendance retrieved for " + effectiveDate, attendance));
    }
}
