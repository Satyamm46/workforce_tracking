package com.institute.workforce_tracking.enums;

/**
 * Which submission deadline an admin-granted extension applies to.
 */
public enum DeadlineType {

    /** Next-day work plan (normally due the day before). */
    WORK_PLAN,

    /** End-of-day work report (normally due within 24h of checkout). */
    WORK_REPORT,

    /** Post-lecture summary (normally due within 24h of the lecture ending). */
    LECTURE_SUMMARY
}
