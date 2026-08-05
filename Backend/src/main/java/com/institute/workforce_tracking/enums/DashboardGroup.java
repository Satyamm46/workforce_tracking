package com.institute.workforce_tracking.enums;

/**
 * The set of people behind one dashboard statistic.
 *
 * <p>Each constant pairs with a count in
 * {@link com.institute.workforce_tracking.dto.response.DashboardStatsResponse},
 * so clicking a tile can answer "who?" — a count alone tells an admin that
 * three people are on a break but not which three.</p>
 */
public enum DashboardGroup {

    /** Every active account. */
    TOTAL,

    /** Currently working or on a break. */
    ONLINE,

    /** Currently working. */
    WORKING,

    /** Currently on a break. */
    ON_BREAK,

    /** Teachers whose lecture is in progress right now. */
    IN_LECTURE,

    /** On approved leave today. */
    ON_LEAVE,

    /** Finished their day. */
    CHECKED_OUT,

    /** Active accounts with no attendance record today. */
    ABSENT
}
