package com.institute.workforce_tracking.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

/**
 * Inbound payload for rescheduling a missed (or cancelled) lecture to a new
 * slot. Subject, class, and batch are carried over from the original.
 *
 * @param lectureDate the new day
 * @param startTime   the new start time
 * @param endTime     the new end time
 */
public record RescheduleLectureRequest(

        @NotNull(message = "Lecture date is required")
        @FutureOrPresent(message = "Lecture date cannot be in the past")
        LocalDate lectureDate,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime
) {
}
