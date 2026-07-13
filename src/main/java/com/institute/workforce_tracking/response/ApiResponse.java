package com.institute.workforce_tracking.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

/**
 * Standard success envelope returned by every REST endpoint in the system.
 *
 * <p>Wrapping every response in a single, predictable shape means the React
 * frontend can rely on {@code success}, {@code message}, and {@code data}
 * being present on <em>every</em> call — no guessing per endpoint.</p>
 *
 * <p>Instances are created through the static factory methods
 * ({@link #success}, {@link #of}) rather than a public constructor, so the
 * envelope is effectively immutable once built.</p>
 *
 * @param <T> the type of the payload carried in {@code data}
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** Always {@code true} for this envelope; errors use ErrorResponse instead. */
    private final boolean success;

    /** Human-readable, client-facing description of the outcome. */
    private final String message;

    /** The actual payload. May be {@code null} (e.g. a delete with no body). */
    private final T data;

    /** Server-side UTC instant the response was produced. */
    private final Instant timestamp;

    /**
     * Private constructor — instances are built only via the static
     * factory methods below. This keeps construction consistent and the
     * object immutable.
     */
    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }

    /**
     * Full-control factory: custom message plus payload.
     *
     * @param message client-facing message
     * @param data    the payload
     * @param <T>     payload type
     * @return a success envelope wrapping {@code data}
     */
    public static <T> ApiResponse<T> of(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Convenience factory: payload with a default success message.
     *
     * @param data the payload
     * @param <T>  payload type
     * @return a success envelope wrapping {@code data}
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Request processed successfully", data);
    }

    /**
     * Convenience factory: message only, no payload (e.g. delete/void actions).
     *
     * @param message client-facing message
     * @param <T>     payload type (unused)
     * @return a success envelope with {@code data == null}
     */
    public static <T> ApiResponse<T> message(String message) {
        return new ApiResponse<>(true, message, null);
    }
}