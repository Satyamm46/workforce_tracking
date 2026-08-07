package com.institute.workforce_tracking.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.institute.workforce_tracking.constants.ApiConstants;
import com.institute.workforce_tracking.dto.request.CreateLectureSeriesRequest;
import com.institute.workforce_tracking.dto.response.ApiResponse;
import com.institute.workforce_tracking.dto.response.LectureSeriesResponse;
import com.institute.workforce_tracking.service.LectureSeriesService;

import jakarta.validation.Valid;

/**
 * REST endpoints for repeating lecture series. Teacher-only: a series belongs
 * to the teacher who defined it, and admins see the resulting lectures on the
 * calendar rather than the templates.
 */
@RestController
@RequestMapping(ApiConstants.LECTURE_SERIES_BASE)
public class LectureSeriesController {

    private final LectureSeriesService lectureSeriesService;

    public LectureSeriesController(LectureSeriesService lectureSeriesService) {
        this.lectureSeriesService = lectureSeriesService;
    }

    /** Creates a repeating series and immediately schedules its occurrences. */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<LectureSeriesResponse>> createSeries(
            Authentication authentication,
            @Valid @RequestBody CreateLectureSeriesRequest request) {

        LectureSeriesResponse series =
                lectureSeriesService.createSeries(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Repeating class created", series));
    }

    /** The authenticated teacher's active series. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<LectureSeriesResponse>>> getMySeries(
            Authentication authentication) {

        List<LectureSeriesResponse> series =
                lectureSeriesService.getMySeries(authentication.getName());
        return ResponseEntity.ok(ApiResponse.of("Repeating classes retrieved", series));
    }

    /** Stops one of the caller's own series and cancels its future classes. */
    @PatchMapping("/{id}/stop")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<LectureSeriesResponse>> stopSeries(
            Authentication authentication,
            @PathVariable Long id) {

        LectureSeriesResponse series =
                lectureSeriesService.stopSeries(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.of("Repeating class stopped", series));
    }
}
