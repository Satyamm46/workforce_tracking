package com.institute.workforce_tracking.service;

/**
 * Self-service password recovery for users who can still reach their inbox but
 * no longer remember their password.
 *
 * <p>A short numeric code is emailed to the address on the account; presenting
 * that code back is what authorizes the new password. Proving control of the
 * inbox stands in for the password the user has lost.</p>
 */
public interface PasswordResetService {

    /**
     * Generates a fresh reset code for the email and sends it. Any previous
     * unconsumed code for the same email is replaced, so only the newest code
     * works.
     *
     * <p>Deliberately silent when no active account matches the address: the
     * caller returns the same response either way, so this endpoint cannot be
     * used to discover which emails have accounts.</p>
     *
     * @param email the address to send the code to
     * @throws com.institute.workforce_tracking.exception.BadRequestException
     *         if an account exists but the message could not be delivered
     */
    void sendCode(String email);

    /**
     * Verifies a reset code and, on success, replaces the account's password
     * and burns the code so it cannot be reused.
     *
     * @param email       the account's email address
     * @param code        the 6-digit code that was emailed
     * @param newPassword the plaintext password to hash and store
     * @throws com.institute.workforce_tracking.exception.BadRequestException
     *         if no code was requested, it expired, too many attempts were
     *         made, or the code does not match
     */
    void resetPassword(String email, String code, String newPassword);
}
