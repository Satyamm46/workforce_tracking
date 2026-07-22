package com.institute.workforce_tracking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for requesting an email verification code (step one of the
 * two-step registration). Only the email is needed to send the code.
 *
 * @param email the address to verify — the code is emailed here
 */
public record SendOtpRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email
) {
}
