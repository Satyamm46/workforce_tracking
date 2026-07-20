package com.institute.workforce_tracking.dto.response;

import com.institute.workforce_tracking.enums.Role;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        Role role,
        boolean enabled
) {
}