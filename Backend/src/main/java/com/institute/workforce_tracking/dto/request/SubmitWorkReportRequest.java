package com.institute.workforce_tracking.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for submitting an end-of-day work report.
 *
 * @param reportText what the user accomplished that day
 * @param workDate   the day being reported on; omit for the most recent
 *                   checkout. An earlier day is only accepted while its
 *                   deadline is still open — normally that means an admin has
 *                   granted a deadline extension for it.
 */
public record SubmitWorkReportRequest(

        @NotBlank(message = "Report text is required")
        @Size(max = 2000, message = "Report text cannot exceed 2000 characters")
        String reportText,

        // Deliberately not @PastOrPresent: that annotation compares against the
        // JVM default zone, while this application computes every date in
        // Asia/Kolkata through DateTimeUtil. On a UTC server the current
        // Kolkata date would be rejected as "future" until 05:30 local. The
        // service rejects unknown days anyway — a date with no checked-out
        // attendance cannot be reported on, which covers future dates.
        LocalDate workDate
) {
}
