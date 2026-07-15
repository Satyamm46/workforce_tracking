package com.institute.workforce_tracking.mapper;

import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.dto.response.RegistrationResponse;
import com.institute.workforce_tracking.entity.RegistrationRequest;

/**
 * Converts {@link RegistrationRequest} entities to their outbound DTO.
 * The stored password hash is never copied.
 */
@Component
public class RegistrationMapper {

    public RegistrationResponse toRegistrationResponse(RegistrationRequest request) {
        return new RegistrationResponse(
                request.getId(),
                request.getFullName(),
                request.getEmail(),
                request.getRequestedRole(),
                request.getStatus(),
                request.getDecidedBy() != null ? request.getDecidedBy().getFullName() : null,
                request.getDecisionComment(),
                request.getCreatedAt()
        );
    }
}
