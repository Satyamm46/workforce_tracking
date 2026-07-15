package com.institute.workforce_tracking.enums;

/**
 * Lifecycle states of a self-registration request.
 *
 * <p>A request is created as {@code PENDING} when someone signs up, and moves
 * to a terminal state ({@code APPROVED} or {@code REJECTED}) when the Super
 * Admin decides on it. Approval is the only path that creates a real
 * {@link com.institute.workforce_tracking.entity.User} account.</p>
 */
public enum RegistrationStatus {

    /** Submitted and awaiting the Super Admin's decision. */
    PENDING,

    /** Approved — a user account was created from this request. */
    APPROVED,

    /** Rejected — no account was created. The email may register again. */
    REJECTED
}
