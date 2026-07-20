package com.institute.workforce_tracking.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.institute.workforce_tracking.constants.ApiConstants;
import com.institute.workforce_tracking.dto.request.SubmitWorkReportRequest;
import com.institute.workforce_tracking.dto.response.ApiResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.dto.response.WorkReportResponse;
import com.institute.workforce_tracking.service.WorkReportService;
import com.institute.workforce_tracking.util.DateTimeUtil;

import jakarta.validation.Valid;

/**
 * REST endpoints for end-of-day work reports. Submission is user-facing;
 * viewing all reports for a day is admin-only.
 */
@RestController
@RequestMapping(ApiConstants.WORK_REPORTS_BASE)
public class WorkReportController {

    private final WorkReportService workReportService;

    public WorkReportController(WorkReportService workReportService) {
        this.workReportService = workReportService;
    }

    /** Submits the caller's work report for their most recent checkout. */
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<WorkReportResponse>> submitReport(
            Authentication authentication,
            @Valid @RequestBody SubmitWorkReportRequest request) {

        WorkReportResponse report =
                workReportService.submitReport(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Work report submitted", report));
    }

    /** The caller's report for a specific day (404 if none). */
    @GetMapping("/me/day")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<WorkReportResponse>> getMyReportForDay(
            Authentication authentication,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate effectiveDate = (date != null) ? date : DateTimeUtil.today();
        WorkReportResponse report =
                workReportService.getMyReportForDay(authentication.getName(), effectiveDate);
        return ResponseEntity.ok(ApiResponse.of("Report retrieved", report));
    }

    /** A page of the caller's reports, newest first. */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<WorkReportResponse>>> getMyReports(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<WorkReportResponse> reports =
                workReportService.getMyReports(authentication.getName(), page, size);
        return ResponseEntity.ok(ApiResponse.of("Reports retrieved", reports));
    }

    /** Admin view: all reports for one day (defaults to today). */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<WorkReportResponse>>> getReportsByDate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LocalDate effectiveDate = (date != null) ? date : DateTimeUtil.today();
        PagedResponse<WorkReportResponse> reports =
                workReportService.getReportsByDate(effectiveDate, page, size);
        return ResponseEntity.ok(
                ApiResponse.of("Reports retrieved for " + effectiveDate, reports));
    }
}
