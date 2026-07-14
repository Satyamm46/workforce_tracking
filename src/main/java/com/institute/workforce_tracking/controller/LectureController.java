package com.institute.workforce_tracking.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.institute.workforce_tracking.constants.ApiConstants;
import com.institute.workforce_tracking.dto.request.ScheduleLectureRequest;
import com.institute.workforce_tracking.dto.response.LectureResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.response.ApiResponse;
import com.institute.workforce_tracking.service.LectureService;
import com.institute.workforce_tracking.util.DateTimeUtil;

import jakarta.validation.Valid;

/**
 * REST endpoints for lecture scheduling. Scheduling and cancellation are
 * teacher-only; the day view is for admins.
 */
@RestController
@RequestMapping(ApiConstants.LECTURES_BASE)
public class LectureController {

    private final LectureService lectureService;

    public LectureController(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    /** Schedules a new lecture for the authenticated teacher. */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<LectureResponse>> scheduleLecture(
            Authentication authentication,
            @Valid @RequestBody ScheduleLectureRequest request) {

        LectureResponse lecture =
                lectureService.scheduleLecture(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Lecture scheduled", lecture));
    }

    /** A page of the authenticated teacher's lectures from today onward. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<PagedResponse<LectureResponse>>> getMyUpcomingLectures(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<LectureResponse> lectures =
                lectureService.getMyUpcomingLectures(authentication.getName(), page, size);
        return ResponseEntity.ok(ApiResponse.of("Upcoming lectures retrieved", lectures));
    }

    /** Cancels one of the caller's own scheduled lectures. */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<LectureResponse>> cancelLecture(
            Authentication authentication,
            @PathVariable Long id) {

        LectureResponse lecture =
                lectureService.cancelLecture(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.of("Lecture cancelled", lecture));
    }

    /** Admin view: all lectures on one day (defaults to today). */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<LectureResponse>>> getLecturesByDate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LocalDate effectiveDate = (date != null) ? date : DateTimeUtil.today();
        PagedResponse<LectureResponse> lectures =
                lectureService.getLecturesByDate(effectiveDate, page, size);
        return ResponseEntity.ok(ApiResponse.of("Lectures retrieved for " + effectiveDate, lectures));
    }
}
