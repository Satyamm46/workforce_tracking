package com.institute.workforce_tracking.dto.request;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import com.institute.workforce_tracking.enums.RecurrenceFrequency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for creating a repeating lecture series.
 *
 * <p>Only presence and size are validated here. Every rule that depends on
 * "now" — the start date not being in the past, how far ahead an end date may
 * reach — lives in the service, because Bean Validation's temporal constraints
 * ({@code @FutureOrPresent} and friends) compare against the JVM's default
 * zone rather than the application's, and would disagree with
 * {@link com.institute.workforce_tracking.util.DateTimeUtil} on either side of
 * midnight.</p>
 *
 * @param subject   subject taught by every occurrence
 * @param className the class every occurrence is for
 * @param batch     optional batch within the class
 * @param startTime start time of day, shared by every occurrence
 * @param endTime   end time of day, shared by every occurrence
 * @param frequency weekly or monthly
 * @param weekdays  which weekdays a WEEKLY series lands on; ignored for MONTHLY
 * @param startDate first day the series may produce an occurrence
 * @param endDate   last eligible day; null for an open-ended series
 */
public record CreateLectureSeriesRequest(

        @NotBlank(message = "Subject is required")
        @Size(max = 100, message = "Subject must not exceed 100 characters")
        String subject,

        @NotBlank(message = "Class is required")
        @Size(max = 100, message = "Class must not exceed 100 characters")
        String className,

        @Size(max = 50, message = "Batch must not exceed 50 characters")
        String batch,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        @NotNull(message = "Repeat frequency is required")
        RecurrenceFrequency frequency,

        Set<DayOfWeek> weekdays,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        LocalDate endDate
) {
}
