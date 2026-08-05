package com.institute.workforce_tracking.service;

import java.util.List;

import com.institute.workforce_tracking.dto.response.DashboardMemberResponse;
import com.institute.workforce_tracking.dto.response.DashboardStatsResponse;
import com.institute.workforce_tracking.enums.DashboardGroup;

/**
 * Aggregated live statistics for the admin dashboard.
 */
public interface DashboardService {

    /** Computes a fresh snapshot of today's workforce statistics. */
    DashboardStatsResponse getStats();

    /**
     * The people counted by one statistic, so a tile can be opened to see who
     * is behind the number.
     *
     * <p>Computed from the same source as the counts, so the list length
     * matches the tile — barring a state change between the two calls.</p>
     *
     * @param group which statistic to expand
     * @return the matching people, ordered by name
     */
    List<DashboardMemberResponse> getMembers(DashboardGroup group);
}
