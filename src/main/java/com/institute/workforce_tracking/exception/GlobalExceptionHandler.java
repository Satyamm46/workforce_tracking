package com.institute.workforce_tracking.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.institute.workforce_tracking.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

/**
 * Centralized exception handling for the entire application.
 *
 * <p>Annotated with {@link RestControllerAdvice}, this class intercepts
 * exceptions thrown by any controller (and the layers beneath it) and
 * converts them into the standard {@link ErrorResponse} shape. This keeps
 * error handling in ONE place — controllers and services never build error
 * responses themselves.</p>
 *
 * <p>Handlers are ordered from most specific to most general. Spring picks
 * the most specific matching handler for a thrown exception.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles all of our custom business exceptions in one place.
     *
     * <p>Because every custom exception extends {@link BusinessException} and
     * carries its own status + error code, we can translate the entire family
     * with a single handler — no per-exception method needed.</p>
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        log.warn("Business exception at [{}]: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse body = ErrorResponse.of(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getStatus().value(),
                request.getRequestURI());

        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    /**
     * Handles validation failures on {@code @Valid} request bodies (DTOs).
     *
     * <p>Spring throws {@link MethodArgumentNotValidException} when a
     * {@code @RequestBody @Valid} object fails its annotations. We collect
     * every field error into the {@code details} list so the client can show
     * per-field feedback.</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .toList();

        log.warn("Validation failed at [{}]: {}", request.getRequestURI(), details);

        ErrorResponse body = ErrorResponse.of(
                "Validation failed",
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                details);

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles validation failures on {@code @RequestParam} / {@code @PathVariable}
     * constraints (e.g. {@code @Min}, {@code @NotBlank} directly on method
     * parameters), which surface as {@link ConstraintViolationException}.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<String> details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        log.warn("Constraint violation at [{}]: {}", request.getRequestURI(), details);

        ErrorResponse body = ErrorResponse.of(
                "Validation failed",
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                details);

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Last-resort handler for any exception not caught above.
     *
     * <p>Returns a generic 500 so we never leak stack traces or internal
     * details to the client. The full exception is logged server-side at
     * ERROR level with its stack trace for diagnosis.</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error at [{}]", request.getRequestURI(), ex);

        ErrorResponse body = ErrorResponse.of(
                "An unexpected error occurred. Please try again later.",
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI());

        return ResponseEntity.internalServerError().body(body);
    }

    /**
     * Formats a single field error as "fieldName: message".
     */
    private static String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}