package com.institute.workforce_tracking.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Base type for all custom, business-level exceptions in the system.
 *
 * <p>Every application-specific exception extends this class so the global
 * exception handler can treat them uniformly: each one carries the HTTP
 * status it should map to and a stable, machine-readable error code.</p>
 *
 * <p>It extends {@link RuntimeException} (unchecked) on purpose — business
 * failures should propagate up to the handler without forcing every service
 * method to declare {@code throws} clauses.</p>
 */
@Getter
public abstract class BusinessException extends RuntimeException {

    /** The HTTP status this exception should translate to. */
    private final HttpStatus status;

    /** Stable, machine-readable code (e.g. "RESOURCE_NOT_FOUND"). */
    private final String errorCode;

    /**
     * @param message   human-readable, client-facing description
     * @param status    the HTTP status this exception maps to
     * @param errorCode stable machine-readable error code
     */
    protected BusinessException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}