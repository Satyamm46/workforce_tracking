package com.institute.workforce_tracking.event;

import com.institute.workforce_tracking.enums.Role;

/**
 * Domain event published after a new self-registration request is persisted.
 * Listeners notify the Super Admin(s) in-app and by email.
 *
 * @param registrationId the new request's id
 * @param fullName       the applicant's name
 * @param email          the applicant's email
 * @param requestedRole  the role the applicant asked for
 */
public record RegistrationSubmittedEvent(
        Long registrationId,
        String fullName,
        String email,
        Role requestedRole
) {
}
