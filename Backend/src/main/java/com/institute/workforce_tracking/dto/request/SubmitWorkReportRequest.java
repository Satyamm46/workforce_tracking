package com.institute.workforce_tracking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for submitting an end-of-day work report.
 *
 * @param reportText what the user accomplished today
 */
public record SubmitWorkReportRequest(

        @NotBlank(message = "Report text is required")
        @Size(max = 2000, message = "Report text cannot exceed 2000 characters")
        String reportText
) {
}
