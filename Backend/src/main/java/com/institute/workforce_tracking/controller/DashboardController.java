package com.institute.workforce_tracking.controller;

import com.institute.workforce_tracking.constants.ApiConstants;
import com.institute.workforce_tracking.dto.response.DashboardStatsResponse;
import com.institute.workforce_tracking.dto.response.ApiResponse;
import com.institute.workforce_tracking.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for the admin dashboard's initial statistics snapshot.
 * Live updates after page load are pushed over the WebSocket topic by
 * {@link com.institute.workforce_tracking.scheduler.DashboardBroadcastScheduler}.
 */
@RestController
@RequestMapping(ApiConstants.DASHBOARD_BASE)
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        DashboardStatsResponse stats = dashboardService.getStats();
        return ResponseEntity.ok(ApiResponse.of("Dashboard stats retrieved", stats));
    }
}
