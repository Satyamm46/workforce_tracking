package com.institute.workforce_tracking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for submitting a post-lecture summary.
 *
 * @param summaryText what was covered in the lecture
 */
public record SubmitLectureSummaryRequest(

        @NotBlank(message = "Summary text is required")
        @Size(max = 2000, message = "Summary text cannot exceed 2000 characters")
        String summaryText
) {
}
