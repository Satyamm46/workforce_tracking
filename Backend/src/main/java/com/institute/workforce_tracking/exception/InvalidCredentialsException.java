package com.institute.workforce_tracking.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when authentication fails — wrong password, unknown email, or a
 * disabled account. Maps to HTTP 401 (Unauthorized).
 *
 * <p>The message is intentionally generic ("Invalid email or password") to
 * avoid revealing WHICH part failed, preventing user-enumeration attacks.</p>
 */
public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }
}