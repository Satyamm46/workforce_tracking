package com.institute.workforce_tracking.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for scheduling a lecture.
 *
 * <p>Cross-field rules (end after start, conflict-free slot) are enforced in
 * the service. {@code batch} is optional — absent or blank means the class
 * has no batch division.</p>
 *
 * @param subject     subject being taught
 * @param className   the class this lecture is for
 * @param batch       optional batch within the class
 * @param lectureDate the day (today or later)
 * @param startTime   start time of day
 * @param endTime     end time of day
 */
public record ScheduleLectureRequest(

        @NotBlank(message = "Subject is required")
        @Size(max = 100, message = "Subject must not exceed 100 characters")
        String subject,

        @NotBlank(message = "Class is required")
        @Size(max = 100, message = "Class must not exceed 100 characters")
        String className,

        @Size(max = 50, message = "Batch must not exceed 50 characters")
        String batch,

        @NotNull(message = "Lecture date is required")
        @FutureOrPresent(message = "Lecture date cannot be in the past")
        LocalDate lectureDate,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime
) {
}
