package com.institute.workforce_tracking.enums;

/**
 * How often a {@link com.institute.workforce_tracking.entity.LectureSeries}
 * repeats.
 *
 * <p>Deliberately small: a teaching timetable is either "these weekdays, every
 * week" or "this date, every month". Anything richer (every second week,
 * "last Friday of the month") belongs in a recurrence-rule parser, not an
 * enum, and is not needed here.</p>
 */
public enum RecurrenceFrequency {

    /** Repeats on one or more chosen weekdays, every week. */
    WEEKLY,

    /** Repeats on the same day of the month, every month. */
    MONTHLY
}
