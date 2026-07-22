package com.institute.workforce_tracking.dto.request;

import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for submitting (or updating) tomorrow's work plan.
 * The plan date itself is decided by the server, never by the client.
 *
 * @param plannedStartTime intended start-of-work time
 * @param plannedEndTime   intended end-of-work time
 * @param workDescription  what the user will work on
 */
public record WorkPlanRequest(

        @NotNull(message = "Planned start time is required")
        LocalTime plannedStartTime,

        @NotNull(message = "Planned end time is required")
        LocalTime plannedEndTime,

        @NotBlank(message = "Work description is required")
        @Size(max = 1000, message = "Work description must not exceed 1000 characters")
        String workDescription
) {
}
