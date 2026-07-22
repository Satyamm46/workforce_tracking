package com.institute.workforce_tracking.dto.request;

import java.time.LocalDate;

import com.institute.workforce_tracking.enums.DeadlineType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for granting a deadline extension.
 *
 * @param userId     the user whose deadline is extended
 * @param type       which deadline (work plan, work report, lecture summary)
 * @param targetDate the day the deadline belongs to
 * @param extraHours grace hours to add; null defaults to 48, capped at 48
 * @param reason     optional reason shown to the user
 */
public record GrantDeadlineExtensionRequest(

        @NotNull(message = "User id is required")
        Long userId,

        @NotNull(message = "Deadline type is required")
        DeadlineType type,

        @NotNull(message = "Target date is required")
        LocalDate targetDate,

        @Min(value = 1, message = "Extension must be at least 1 hour")
        @Max(value = 48, message = "Extension cannot exceed 48 hours")
        Integer extraHours,

        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason
) {
}
