package com.institute.workforce_tracking.service;

import com.institute.workforce_tracking.dto.response.DashboardStatsResponse;

/**
 * Aggregated live statistics for the admin dashboard.
 */
public interface DashboardService {

    /** Computes a fresh snapshot of today's workforce statistics. */
    DashboardStatsResponse getStats();
}
