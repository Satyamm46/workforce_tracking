package com.institute.workforce_tracking.dto.request;

import com.institute.workforce_tracking.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for the public self-registration form.
 *
 * @param fullName      the applicant's display name
 * @param email         the email they will log in with once approved
 * @param password      the password they choose now (hashed before storage)
 * @param requestedRole the role they are asking for (never SUPER_ADMIN)
 */
public record RegisterRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password,

        @NotNull(message = "Requested role is required")
        Role requestedRole
) {
}
