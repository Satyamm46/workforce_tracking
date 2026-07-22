package com.institute.workforce_tracking.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when creating a resource that would violate a uniqueness rule —
 * e.g. registering a user with an email that already exists.
 *
 * <p>Maps to HTTP 409 (Conflict): the request is well-formed, but conflicts
 * with the current state of the server.</p>
 */
public class DuplicateResourceException extends BusinessException {

    private static final String ERROR_CODE = "DUPLICATE_RESOURCE";

    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT, ERROR_CODE);
    }

    /**
     * Builds a consistent message from the resource, field, and conflicting value.
     *
     * @param resourceName the resource type (e.g. "User")
     * @param fieldName    the unique field (e.g. "email")
     * @param fieldValue   the value that already exists
     */
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.CONFLICT, ERROR_CODE);
    }
}