package com.institute.workforce_tracking.constants;

/**
 * Application-wide constant values that are not tied to any single feature.
 *
 * <p>Centralizing "magic" values here — pagination defaults, the canonical
 * time zone, shared formats — means they are defined once and referenced
 * everywhere, so a change is made in exactly one place.</p>
 *
 * <p>This is a non-instantiable holder of static constants: it is
 * {@code final} with a private constructor.</p>
 */
public final class AppConstants {

    private AppConstants() {
        // Prevent instantiation — this class only holds constants.
    }

    /**
     * Canonical time zone for the whole application. MUST match the
     * {@code spring.jackson.time-zone} value and the JDBC {@code serverTimezone}
     * so the app, JSON output, and database agree on time.
     */
    public static final String DEFAULT_TIME_ZONE = "Asia/Kolkata";

    /** Standard date format used across the system (e.g. 2026-07-14). */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /** Standard date-time format used across the system. */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** Default page number (zero-based) for paginated endpoints. */
    public static final int DEFAULT_PAGE_NUMBER = 0;

    /** Default page size for paginated endpoints. */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** Maximum page size a client may request, to protect the database. */
    public static final int MAX_PAGE_SIZE = 100;

    /** Default sort direction for paginated endpoints. */
    public static final String DEFAULT_SORT_DIRECTION = "asc";
}