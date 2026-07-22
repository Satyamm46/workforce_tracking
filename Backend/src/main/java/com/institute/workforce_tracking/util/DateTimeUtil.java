package com.institute.workforce_tracking.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.institute.workforce_tracking.constants.AppConstants;

/**
 * Centralized, time-zone-aware helpers for date/time operations.
 *
 * <p>In an attendance system, time is domain-critical: a clock-in stored in
 * the wrong zone is a wrong record. Routing all "now" and formatting logic
 * through this single class guarantees the <em>entire</em> application uses
 * one consistent zone ({@link AppConstants#DEFAULT_TIME_ZONE}) and one set of
 * formats — never a scattered mix of {@code LocalDateTime.now()} calls that
 * silently pick up the server's default zone.</p>
 *
 * <p>Stateless, non-instantiable utility: {@code final} with a private
 * constructor and only static members.</p>
 */
public final class DateTimeUtil {

    /** The single ZoneId the whole application computes time in. */
    private static final ZoneId ZONE = ZoneId.of(AppConstants.DEFAULT_TIME_ZONE);

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT);

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(AppConstants.DATE_TIME_FORMAT);

    private DateTimeUtil() {
        // Prevent instantiation — this class only holds static helpers.
    }

    /**
     * @return the application ZoneId (single source of truth for the zone)
     */
    public static ZoneId zone() {
        return ZONE;
    }

    /**
     * @return the current date in the application's time zone
     */
    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    /**
     * @return the current date-time in the application's time zone
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }

    /**
     * @return the current zoned date-time (carries the zone explicitly)
     */
    public static ZonedDateTime nowZoned() {
        return ZonedDateTime.now(ZONE);
    }

    /**
     * Formats a date using the application's standard date pattern.
     *
     * @param date the date to format; may be {@code null}
     * @return the formatted date, or {@code null} if {@code date} is null
     */
    public static String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMATTER);
    }

    /**
     * Formats a date-time using the application's standard pattern.
     *
     * @param dateTime the value to format; may be {@code null}
     * @return the formatted date-time, or {@code null} if {@code dateTime} is null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATE_TIME_FORMATTER);
    }
}