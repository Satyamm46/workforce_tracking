package com.institute.workforce_tracking.dto.response;

/**
 * A snapshot of live workforce statistics for the admin dashboard.
 *
 * <p>Being a record, two snapshots with identical counts are {@code equals()}
 * — the broadcast scheduler relies on this to skip pushing unchanged stats.</p>
 *
 * @param totalEmployees   active user accounts
 * @param onlineCount      currently working or on a break
 * @param workingCount     currently working
 * @param onBreakCount     currently on a break
 * @param checkedOutCount  finished their day
 * @param onLeaveCount     on approved leave today
 * @param absentCount      active users with no attendance record today
 * @param liveLectureCount lectures in progress right now
 */
public record DashboardStatsResponse(
        long totalEmployees,
        long onlineCount,
        long workingCount,
        long onBreakCount,
        long checkedOutCount,
        long onLeaveCount,
        long absentCount,
        long liveLectureCount
) {
}
