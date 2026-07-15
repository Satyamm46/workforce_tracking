package com.institute.workforce_tracking.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.institute.workforce_tracking.dto.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

/**
 * Centralized exception handling for the entire application.
 *
 * <p>Intercepts exceptions thrown by any controller (and the layers beneath it)
 * and converts them into the standard {@link ErrorResponse} shape, so every
 * error in the system looks identical to the client.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles all custom business exceptions (ResourceNotFound, BadRequest,
     * InvalidCredentials, DuplicateResource, …) in one place — each carries its
     * own HTTP status and error code.
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
     * Handles validation failures on {@code @Valid @RequestBody} DTOs,
     * collecting every field error into the details list.
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
     * constraints, which surface as ConstraintViolationException.
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
     * Handles malformed request bodies — invalid JSON, or a value that cannot
     * be deserialized (e.g. an unknown enum constant for a role). This is the
     * client's error, so it maps to 400 rather than the catch-all 500.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Malformed request body at [{}]: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse body = ErrorResponse.of(
                "Request body is malformed or contains an invalid value.",
                "MALFORMED_REQUEST",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI());

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Last-resort handler for any exception not caught above. Returns a generic
     * 500 without leaking internal details; the full stack trace is logged.
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