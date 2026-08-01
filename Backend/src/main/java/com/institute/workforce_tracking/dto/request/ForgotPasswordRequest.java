package com.institute.workforce_tracking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for step one of password recovery — requesting a reset code.
 * Only the email is needed; the code is sent to that inbox.
 *
 * @param email the address of the account whose password is being reset
 */
public record ForgotPasswordRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email
) {
}
