package com.institute.workforce_tracking.service;

import java.time.LocalDate;

import com.institute.workforce_tracking.dto.request.SubmitLectureSummaryRequest;
import com.institute.workforce_tracking.dto.response.LectureSummaryResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;

/**
 * Business logic for post-lecture summaries, required within 24 hours of
 * lecture completion. Missing summaries trigger lecture cancellation.
 */
public interface LectureSummaryService {

    /**
     * Submits the caller's summary for a specific completed lecture.
     * Throws BadRequest if the lecture is not COMPLETED or a summary was
     * already submitted.
     */
    LectureSummaryResponse submitSummary(String teacherEmail, Long lectureId,
                                         SubmitLectureSummaryRequest request);

    /** The caller's summary for a specific lecture (404 if none). */
    LectureSummaryResponse getMySummaryForLecture(String teacherEmail, Long lectureId);

    /** A page of the caller's summaries, newest first. */
    PagedResponse<LectureSummaryResponse> getMySummaries(String teacherEmail, int page, int size);

    /** Manager view: all summaries for lectures on one day. */
    PagedResponse<LectureSummaryResponse> getSummariesByDate(LocalDate date, int page, int size);

    /** Manager view: all summaries for lectures within [from, to] — backs the monthly export. */
    PagedResponse<LectureSummaryResponse> getSummariesByDateRange(
            LocalDate from, LocalDate to, int page, int size);

    /**
     * Scheduler-invoked sweep: cancels completed lectures whose actual end
     * was ≥24h ago without a submitted summary (teachers only).
     * Returns count of lectures cancelled.
     */
    int cancelLecturesWithMissingSummaries();
}
