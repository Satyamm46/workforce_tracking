package com.institute.workforce_tracking.service;

/**
 * Email-ownership verification for public self-registration.
 *
 * <p>A short numeric code is emailed to the address someone signs up with;
 * registration only proceeds once that code is presented back, proving the
 * applicant controls the inbox. This blocks registrations against addresses
 * the applicant does not own (fake or someone else's).</p>
 */
public interface EmailVerificationService {

    /**
     * Generates a fresh code for the email and sends it. Any previous
     * unconsumed code for the same email is replaced. Throws if the email
     * could not be sent, so the caller can tell the applicant.
     *
     * @param email the address to verify
     * @throws com.institute.workforce_tracking.exception.BadRequestException
     *         if the message could not be delivered
     */
    void sendCode(String email);

    /**
     * Verifies and consumes a code for the given email. On success the stored
     * verification is marked used so the same code cannot be replayed.
     *
     * @param email the address being verified
     * @param code  the 6-digit code the applicant entered
     * @throws com.institute.workforce_tracking.exception.BadRequestException
     *         if no code was requested, it expired, too many attempts were
     *         made, or the code does not match
     */
    void verifyCode(String email, String code);
}
