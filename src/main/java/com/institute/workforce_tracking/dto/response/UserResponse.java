package com.institute.workforce_tracking.dto.response;

import com.institute.workforce_tracking.enums.Role;

/**
 * Safe, outbound representation of a user.
 *
 * <p>Deliberately omits the password hash and audit internals — it carries
 * only what a client may see. This is the DTO that prevents sensitive entity
 * fields from ever leaking into an API response.</p>
 *
 * @param id       the user's id
 * @param fullName the user's display name
 * @param email    the user's email
 * @param role     the user's role
 */
public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role
) {
}