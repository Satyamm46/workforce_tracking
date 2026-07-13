package com.institute.workforce_tracking.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a request is semantically invalid beyond what bean
 * validation can express (e.g. a business rule violation).
 *
 * <p>Maps to HTTP 400 (Bad Request). Use this for rule violations the
 * annotations on a DTO cannot capture, such as "clock-out time must be
 * after clock-in time".</p>
 */
public class BadRequestException extends BusinessException {

    private static final String ERROR_CODE = "BAD_REQUEST";

    /**
     * @param message description of why the request is invalid
     */
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ERROR_CODE);
    }
}