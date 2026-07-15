package com.institute.workforce_tracking.dto.request;

import com.institute.workforce_tracking.enums.Role;

import jakarta.validation.constraints.Size;

/**
 * Inbound payload for the Super Admin's decision on a registration request.
 *
 * @param role    optional role override on approval; when null, the role the
 *                applicant requested is used
 * @param comment optional decision comment (e.g. a rejection reason)
 */
public record RegistrationDecisionRequest(

        Role role,

        @Size(max = 500, message = "Comment must not exceed 500 characters")
        String comment
) {
}
