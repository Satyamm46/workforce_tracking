package com.institute.workforce_tracking.dto.response;

import java.time.Instant;

import com.institute.workforce_tracking.enums.RegistrationStatus;
import com.institute.workforce_tracking.enums.Role;

/**
 * Outbound representation of a registration request. The stored password hash
 * is deliberately never exposed.
 */
public record RegistrationResponse(
        Long id,
        String fullName,
        String email,
        Role requestedRole,
        RegistrationStatus status,
        String decidedByName,
        String decisionComment,
        Instant createdAt
) {
}
