package com.institute.workforce_tracking.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for applying for leave.
 *
 * <p>Structural rules live here as annotations. Cross-field and stateful rules
 * (end after start, no overlap, sufficient balance) belong to the service.</p>
 *
 * @param startDate first day of leave (inclusive; today or later)
 * @param endDate   last day of leave (inclusive)
 * @param reason    the employee's stated reason
 */
public record ApplyLeaveRequest(

        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date cannot be in the past")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        @NotBlank(message = "Reason is required")
        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
