package com.institute.workforce_tracking.enums;

/**
 * Lifecycle of a lecture.
 *
 * <p>A lecture goes LIVE when the teacher presses Start Class (possibly
 * later than scheduled — the session shifts accordingly). One that is never
 * started by its scheduled end is marked MISSED by the scheduler.</p>
 */
public enum LectureStatus {

    /** Planned; waiting for the teacher to start it. */
    SCHEDULED,

    /** Currently in progress (started by the teacher). */
    LIVE,

    /** Finished — ended by the teacher or auto-completed. */
    COMPLETED,

    /** Cancelled by the teacher before it started. */
    CANCELLED,

    /** Never started: its scheduled end passed without the teacher starting it. */
    MISSED,

    /** Completed but the teacher never submitted a summary within 24h. */
    SUMMARY_MISSED
}
