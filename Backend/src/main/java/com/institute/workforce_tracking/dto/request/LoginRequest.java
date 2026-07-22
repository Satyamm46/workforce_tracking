package com.institute.workforce_tracking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Inbound payload for a login request.
 *
 * <p>A record because it is an immutable data carrier. Bean Validation
 * annotations declare the structural rules; the controller triggers them with
 * {@code @Valid}, and any violation is turned into a 400 by the existing
 * GlobalExceptionHandler.</p>
 *
 * @param email    the user's login email
 * @param password the user's raw password
 */
public record LoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
}