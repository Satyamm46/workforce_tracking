package com.institute.workforce_tracking.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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
import com.institute.workforce_tracking.dto.request.WorkPlanRequest;
import com.institute.workforce_tracking.dto.response.ApiResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.dto.response.WorkPlanResponse;
import com.institute.workforce_tracking.service.WorkPlanService;
import com.institute.workforce_tracking.util.DateTimeUtil;

import jakarta.validation.Valid;

/**
 * REST endpoints for next-day work plans. Personal endpoints are open to any
 * authenticated user; day-wide views are for managers.
 */
@RestController
@RequestMapping(ApiConstants.WORK_PLANS_BASE)
public class WorkPlanController {

    private final WorkPlanService workPlanService;

    public WorkPlanController(WorkPlanService workPlanService) {
        this.workPlanService = workPlanService;
    }

    /** Creates or updates the caller's plan for tomorrow. */
    @PostMapping("/tomorrow")
    public ResponseEntity<ApiResponse<WorkPlanResponse>> submitTomorrow(
            @Valid @RequestBody WorkPlanRequest request,
            Authentication authentication) {

        WorkPlanResponse plan = workPlanService.submitTomorrowPlan(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.of("Tomorrow's plan saved", plan));
    }

    /** Late escape hatch: creates the caller's missing plan for TODAY. */
    @PostMapping("/today")
    public ResponseEntity<ApiResponse<WorkPlanResponse>> submitTodayLate(
            @Valid @RequestBody WorkPlanRequest request,
            Authentication authentication) {

        WorkPlanResponse plan = workPlanService.submitTodayPlanLate(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.of("Today's plan saved (marked late)", plan));
    }

    /** The caller's plan for one day (defaults to today). */
    @GetMapping("/me/day")
    public ResponseEntity<ApiResponse<WorkPlanResponse>> getMyPlanForDay(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {

        LocalDate effectiveDate = (date != null) ? date : DateTimeUtil.today();
        WorkPlanResponse plan = workPlanService.getMyPlan(authentication.getName(), effectiveDate);
        return ResponseEntity.ok(ApiResponse.of("Plan retrieved for " + effectiveDate, plan));
    }

    /** A page of the caller's plans, newest day first. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PagedResponse<WorkPlanResponse>>> getMyPlans(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<WorkPlanResponse> plans =
                workPlanService.getMyPlans(authentication.getName(), page, size);
        return ResponseEntity.ok(ApiResponse.of("Plans retrieved", plans));
    }

    /** Manager view: all plans submitted for one day (defaults to today). */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<WorkPlanResponse>>> getPlansByDate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LocalDate effectiveDate = (date != null) ? date : DateTimeUtil.today();
        PagedResponse<WorkPlanResponse> plans =
                workPlanService.getPlansByDate(effectiveDate, page, size);
        return ResponseEntity.ok(ApiResponse.of("Plans retrieved for " + effectiveDate, plans));
    }

    /** Manager view: who has NOT submitted a plan for one day (defaults to today). */
    @GetMapping("/missing")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<String>>> getMissingSubmitters(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate effectiveDate = (date != null) ? date : DateTimeUtil.today();
        List<String> missing = workPlanService.getMissingSubmitters(effectiveDate);
        return ResponseEntity.ok(ApiResponse.of("Missing submitters for " + effectiveDate, missing));
    }
}
