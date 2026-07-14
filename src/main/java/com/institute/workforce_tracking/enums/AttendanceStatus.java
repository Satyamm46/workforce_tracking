package com.institute.workforce_tracking.enums;

/**
 * The state of an employee's working day.
 *
 * <p>Drives both the per-record lifecycle (working → checked out) and the
 * live workforce view (who is currently doing what).</p>
 */
public enum AttendanceStatus {

    /** Logged in and currently working. */
    WORKING,

    /**
     * On a break. Reserved for the break-management milestone — no code sets
     * this value yet, but modeling it now keeps the lifecycle complete.
     */
    ON_BREAK,

    /** The working day has ended (clocked out). */
    CHECKED_OUT
}