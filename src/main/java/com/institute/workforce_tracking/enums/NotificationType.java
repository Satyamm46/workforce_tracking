package com.institute.workforce_tracking.enums;

/**
 * Categories of in-app notifications. The frontend uses the type to pick an
 * icon/colour; future channels (email) can route by it. New triggers (break
 * reminders, attendance reminders) extend this enum without structural change.
 */
public enum NotificationType {

    /** A live lecture is approaching its effective end. */
    LECTURE_ENDING,

    /** A teacher's lecture starts in a few minutes. */
    LECTURE_STARTING,

    /** A lecture was cancelled because the teacher never started it. */
    LECTURE_MISSED,

    /** An admin extended one of the user's submission deadlines. */
    DEADLINE_EXTENDED,

    /** A leave request was approved. */
    LEAVE_APPROVED,

    /** A leave request was rejected. */
    LEAVE_REJECTED,

    /** A new self-registration is awaiting the Super Admin's approval. */
    REGISTRATION_SUBMITTED,

    /** The system placed the user on a break because they disconnected. */
    AUTO_BREAK_STARTED,

    /** The system checked the user out after their auto-break hit the limit. */
    AUTO_CHECKED_OUT,

    /** Checked in past the grace period after the planned start — half day. */
    LATE_ARRIVAL
}
