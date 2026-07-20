package com.institute.workforce_tracking.service;

import java.time.LocalDate;

import com.institute.workforce_tracking.dto.request.SubmitWorkReportRequest;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.dto.response.WorkReportResponse;

/**
 * Business logic for end-of-day work reports, required within 24 hours of
 * checkout (for EMPLOYEE, ADMIN, SUPER_ADMIN). Missing reports trigger
 * absence marking.
 */
public interface WorkReportService {

    /**
     * Submits the caller's work report for the most recent checked-out day.
     * Throws BadRequest if no checked-out attendance exists or a report was
     * already submitted for that day.
     */
    WorkReportResponse submitReport(String email, SubmitWorkReportRequest request);

    /** The caller's report for a specific day (404 if none). */
    WorkReportResponse getMyReportForDay(String email, LocalDate date);

    /** A page of the caller's reports, newest first. */
    PagedResponse<WorkReportResponse> getMyReports(String email, int page, int size);

    /** Manager view: all reports for one day. */
    PagedResponse<WorkReportResponse> getReportsByDate(LocalDate date, int page, int size);

    /**
     * Scheduler-invoked sweep: marks users absent who checked out ≥24h ago
     * without submitting a report (only EMPLOYEE/ADMIN/SUPER_ADMIN).
     * Returns count of absences marked.
     */
    int markAbsentForMissingReports();
}
