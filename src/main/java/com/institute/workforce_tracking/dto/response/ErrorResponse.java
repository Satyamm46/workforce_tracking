package com.institute.workforce_tracking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Standard error envelope returned whenever a request fails.
 *
 * <p>This is the failure-path counterpart to {@link ApiResponse}. Keeping
 * success and error as two separate types means a response is unambiguously
 * one or the other — never a half-populated hybrid.</p>
 *
 * <p>Produced centrally by the global exception handler so that every error
 * in the system — validation, not-found, unhandled — looks identical to the
 * client.</p>
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** Always {@code false} for an error envelope. */
    private final boolean success;

    /** Short, client-facing summary of what went wrong. */
    private final String message;

    /** Machine-readable error code (e.g. "RESOURCE_NOT_FOUND"). */
    private final String error;

    /** HTTP status code as an int (e.g. 404), convenient for the frontend. */
    private final int status;

    /** The request path that produced the error (e.g. "/api/health"). */
    private final String path;

    /**
     * Field-level validation messages. Null for non-validation errors, so
     * {@code @JsonInclude(NON_NULL)} keeps it out of the JSON when unused.
     */
    private final List<String> details;

    /** Server-side UTC instant the error was produced. */
    private final Instant timestamp;

    private ErrorResponse(String message, String error, int status,
                          String path, List<String> details) {
        this.success = false;
        this.message = message;
        this.error = error;
        this.status = status;
        this.path = path;
        this.details = details;
        this.timestamp = Instant.now();
    }

    /**
     * Factory for a simple error without field-level details.
     *
     * @param message client-facing summary
     * @param error   machine-readable error code
     * @param status  HTTP status code
     * @param path    the request path that failed
     * @return an error envelope
     */
    public static ErrorResponse of(String message, String error, int status, String path) {
        return new ErrorResponse(message, error, status, path, null);
    }

    /**
     * Factory for an error carrying field-level validation details.
     *
     * @param message client-facing summary
     * @param error   machine-readable error code
     * @param status  HTTP status code
     * @param path    the request path that failed
     * @param details list of field-level messages
     * @return an error envelope including {@code details}
     */
    public static ErrorResponse of(String message, String error, int status,
                                   String path, List<String> details) {
        return new ErrorResponse(message, error, status, path, details);
    }
}