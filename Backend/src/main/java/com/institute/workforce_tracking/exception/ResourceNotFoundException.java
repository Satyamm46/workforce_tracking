package com.institute.workforce_tracking.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource does not exist.
 *
 * <p>Maps to HTTP 404 (Not Found). Typical use: a lookup by id that returns
 * nothing, e.g. {@code findById(id).orElseThrow(...)} in a service.</p>
 */
public class ResourceNotFoundException extends BusinessException {

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    /**
     * @param message description of what was not found
     */
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    /**
     * Convenience constructor that builds a consistent message from the
     * resource name and the identifier that was searched for.
     *
     * @param resourceName the kind of resource (e.g. "Employee")
     * @param fieldName    the field searched by (e.g. "id")
     * @param fieldValue   the value that was not found
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.NOT_FOUND, ERROR_CODE);
    }
}