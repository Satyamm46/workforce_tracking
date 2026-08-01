package com.institute.workforce_tracking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for step two of password recovery — presenting the emailed
 * code together with the password to set.
 *
 * <p>The password rules mirror {@link RegisterRequest} exactly: a recovery flow
 * must not become a way to slip a weaker password past the sign-up rules.</p>
 *
 * @param email       the address of the account being reset
 * @param otp         the 6-digit code that was emailed
 * @param newPassword the password to store (hashed before it is saved)
 */
public record ResetPasswordRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @NotBlank(message = "Reset code is required")
        @Pattern(regexp = "^[0-9]{6}$", message = "Reset code must be 6 digits")
        String otp,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9]).+$",
                message = "Password must contain at least one letter and one number")
        String newPassword
) {
}
