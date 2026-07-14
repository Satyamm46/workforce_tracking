package com.institute.workforce_tracking.enums;

/**
 * Lifecycle of a leave request.
 *
 * <p>Transitions are guarded by the leave service: a request starts PENDING
 * and moves exactly once — to APPROVED or REJECTED (by an admin) or CANCELLED
 * (by its owner, while still pending). All three outcomes are terminal.</p>
 */
public enum LeaveStatus {

    /** Submitted and awaiting an admin decision. */
    PENDING,

    /** Approved by an admin; attendance records were generated. */
    APPROVED,

    /** Rejected by an admin. */
    REJECTED,

    /** Withdrawn by the requesting employee before a decision. */
    CANCELLED
}
