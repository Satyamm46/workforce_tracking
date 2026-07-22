package com.institute.workforce_tracking.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Inbound payload for extending a live lecture.
 *
 * <p>{@code @Max(30)} bounds a single request; the CUMULATIVE 30-minute cap
 * across multiple extensions is a stateful rule enforced by the service.</p>
 *
 * @param minutes how many minutes to add
 */
public record ExtendLectureRequest(

        @NotNull(message = "Minutes is required")
        @Min(value = 1, message = "Extension must be at least 1 minute")
        @Max(value = 30, message = "Extension cannot exceed 30 minutes")
        Integer minutes
) {
}
