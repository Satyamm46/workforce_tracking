package com.institute.workforce_tracking.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.institute.workforce_tracking.constants.ApiConstants;
import com.institute.workforce_tracking.dto.request.SubmitLectureSummaryRequest;
import com.institute.workforce_tracking.dto.response.ApiResponse;
import com.institute.workforce_tracking.dto.response.LectureSummaryResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.service.LectureSummaryService;
import com.institute.workforce_tracking.util.DateTimeUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiConstants.LECTURE_SUMMARIES_BASE)
public class LectureSummaryController {

    private final LectureSummaryService lectureSummaryService;

    public LectureSummaryController(LectureSummaryService lectureSummaryService) {
        this.lectureSummaryService = lectureSummaryService;
    }

    /** Submits the caller's summary for a specific completed lecture. */
    @PostMapping("/{lectureId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<LectureSummaryResponse>> submitSummary(
            Authentication authentication,
            @PathVariable Long lectureId,
            @Valid @RequestBody SubmitLectureSummaryRequest request) {

        LectureSummaryResponse summary =
                lectureSummaryService.submitSummary(authentication.getName(), lectureId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Lecture summary submitted", summary));
    }

    /** The caller's summary for a specific lecture. */
    @GetMapping("/{lectureId}/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<LectureSummaryResponse>> getMySummaryForLecture(
            Authentication authentication,
            @PathVariable Long lectureId) {

        LectureSummaryResponse summary =
                lectureSummaryService.getMySummaryForLecture(authentication.getName(), lectureId);
        return ResponseEntity.ok(ApiResponse.of("Summary retrieved", summary));
    }

    /** A page of the caller's summaries, newest first. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<PagedResponse<LectureSummaryResponse>>> getMySummaries(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<LectureSummaryResponse> summaries =
                lectureSummaryService.getMySummaries(authentication.getName(), page, size);
        return ResponseEntity.ok(ApiResponse.of("Summaries retrieved", summaries));
    }

    /**
     * Admin view: summaries for lectures on one day, OR across a date range
     * when both {@code from} and {@code to} are supplied (backs the monthly
     * export). Range takes precedence over the single {@code date}.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<LectureSummaryResponse>>> getSummariesByDate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (from != null && to != null) {
            PagedResponse<LectureSummaryResponse> summaries =
                    lectureSummaryService.getSummariesByDateRange(from, to, page, size);
            return ResponseEntity.ok(
                    ApiResponse.of("Summaries retrieved for " + from + " to " + to, summaries));
        }

        LocalDate effectiveDate = (date != null) ? date : DateTimeUtil.today();
        PagedResponse<LectureSummaryResponse> summaries =
                lectureSummaryService.getSummariesByDate(effectiveDate, page, size);
        return ResponseEntity.ok(
                ApiResponse.of("Summaries retrieved for " + effectiveDate, summaries));
    }
}
