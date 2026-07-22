package com.institute.workforce_tracking.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Inbound payload for an admin's approve/reject decision.
 *
 * @param comment optional note to the employee (e.g. a rejection reason)
 */
public record DecisionRequest(

        @Size(max = 500, message = "Comment must not exceed 500 characters")
        String comment
) {
}
