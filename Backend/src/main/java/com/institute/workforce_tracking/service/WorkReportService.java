package com.institute.workforce_tracking.service;

import java.time.LocalDate;

import java.util.List;

import com.institute.workforce_tracking.dto.request.SubmitWorkReportRequest;
import com.institute.workforce_tracking.dto.response.OpenWorkReportDayResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.dto.response.WorkReportResponse;

/**
 * Business logic for end-of-day work reports, required within 24 hours of
 * checkout (for EMPLOYEE, ADMIN, SUPER_ADMIN). Missing reports trigger
 * absence marking.
 */
public interface WorkReportService {

    /**
     * Submits the caller's work report. The request's {@code workDate} picks
     * the day; when it is omitted the most recent checked-out day is used.
     *
     * <p>An earlier day is accepted only while its deadline is still open,
     * which in practice means an admin has extended it. Throws BadRequest if
     * no checked-out attendance exists for the day, a report was already
     * submitted for it, or its window has closed.</p>
     */
    WorkReportResponse submitReport(String email, SubmitWorkReportRequest request);

    /**
     * Days the caller has checked out of, still owes a report for, and can
     * still file one for. Drives the day picker on the submission form —
     * without it an extended older day is unreachable.
     */
    List<OpenWorkReportDayResponse> getOpenReportDays(String email);

    /** The caller's report for a specific day (404 if none). */
    WorkReportResponse getMyReportForDay(String email, LocalDate date);

    /** A page of the caller's reports, newest first. */
    PagedResponse<WorkReportResponse> getMyReports(String email, int page, int size);

    /** Manager view: all reports for one day. */
    PagedResponse<WorkReportResponse> getReportsByDate(LocalDate date, int page, int size);

    /** Manager view: all reports whose work date falls within [start, end] — backs the monthly export. */
    PagedResponse<WorkReportResponse> getReportsByDateRange(
            LocalDate start, LocalDate end, int page, int size);

    /**
     * Scheduler-invoked sweep: marks users absent who checked out ≥24h ago
     * without submitting a report (only EMPLOYEE/ADMIN/SUPER_ADMIN).
     * Returns count of absences marked.
     */
    int markAbsentForMissingReports();
}
