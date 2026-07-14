package com.institute.workforce_tracking.enums;

/**
 * Lifecycle of a lecture.
 *
 * <p>M6 implements SCHEDULED and CANCELLED. LIVE and COMPLETED are declared
 * now — the tracking milestone adds the transitions into them (automatic at
 * start time, teacher-ended, or auto-completed), not the states themselves.</p>
 */
public enum LectureStatus {

    /** Planned; start time is in the future. */
    SCHEDULED,

    /** Currently in progress (set automatically by the tracking scheduler). */
    LIVE,

    /** Finished — ended by the teacher or auto-completed. */
    COMPLETED,

    /** Cancelled by the teacher before it started. */
    CANCELLED
}
