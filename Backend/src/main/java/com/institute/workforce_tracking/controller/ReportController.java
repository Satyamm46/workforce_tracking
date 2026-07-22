package com.institute.workforce_tracking.controller;

import com.institute.workforce_tracking.constants.ApiConstants;
import com.institute.workforce_tracking.dto.response.AttendanceReportRow;
import com.institute.workforce_tracking.dto.response.TeacherReportRow;
import com.institute.workforce_tracking.dto.response.ApiResponse;
import com.institute.workforce_tracking.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for monthly reports. Admin-only.
 */
@RestController
@RequestMapping(ApiConstants.REPORTS_BASE)
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<List<AttendanceReportRow>>> getAttendanceReport(
            @RequestParam int year, @RequestParam int month) {
        List<AttendanceReportRow> report = reportService.getMonthlyAttendanceReport(year, month);
        return ResponseEntity.ok(ApiResponse.of("Attendance report generated", report));
    }

    @GetMapping("/teaching")
    public ResponseEntity<ApiResponse<List<TeacherReportRow>>> getTeachingReport(
            @RequestParam int year, @RequestParam int month) {
        List<TeacherReportRow> report = reportService.getMonthlyTeachingReport(year, month);
        return ResponseEntity.ok(ApiResponse.of("Teaching report generated", report));
    }
}
