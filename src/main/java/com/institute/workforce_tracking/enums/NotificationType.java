package com.institute.workforce_tracking.enums;

/**
 * Categories of in-app notifications. The frontend uses the type to pick an
 * icon/colour; future channels (email) can route by it. New triggers (break
 * reminders, attendance reminders) extend this enum without structural change.
 */
public enum NotificationType {

    /** A live lecture is approaching its effective end. */
    LECTURE_ENDING,

    /** A leave request was approved. */
    LEAVE_APPROVED,

    /** A leave request was rejected. */
    LEAVE_REJECTED
}
