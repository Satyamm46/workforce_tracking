package com.institute.workforce_tracking.dto.request;

import com.institute.workforce_tracking.enums.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for updating a user's editable details.
 *
 * <p>Email is intentionally NOT updatable here (it's the login identity), and
 * account status is handled by dedicated activate/deactivate endpoints — so
 * this DTO covers only the fields an admin edits directly.</p>
 *
 * @param fullName the updated display name
 * @param role     the updated role
 */
public record UpdateUserRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        @NotNull(message = "Role is required")
        Role role
) {
}