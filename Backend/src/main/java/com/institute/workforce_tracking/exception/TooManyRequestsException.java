package com.institute.workforce_tracking.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a caller has exceeded the allowed number of attempts for an
 * endpoint within its rate-limit window.
 *
 * <p>Maps to HTTP 429 (Too Many Requests). Raised by services guarding
 * abuse-prone public endpoints — login (password guessing) and OTP dispatch
 * (mail-quota drain).</p>
 */
public class TooManyRequestsException extends BusinessException {

    private static final String ERROR_CODE = "TOO_MANY_REQUESTS";

    /**
     * @param message client-facing description of the limit that was hit
     */
    public TooManyRequestsException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, ERROR_CODE);
    }
}
