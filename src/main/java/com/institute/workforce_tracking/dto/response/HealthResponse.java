package com.institute.workforce_tracking.dto;

/**
 * Payload describing the application's liveness status.
 *
 * <p>Implemented as a {@code record}: an immutable, transparent data carrier
 * that is ideal for DTOs. It becomes the {@code data} element inside the
 * standard {@link com.institute.workforce_tracking.response.ApiResponse}
 * envelope returned by the health endpoint.</p>
 *
 * @param status a short liveness indicator, e.g. {@code "UP"}
 */
public record HealthResponse(String status) {

    /** Canonical "service is up" instance, reused to avoid re-allocation. */
    public static final HealthResponse UP = new HealthResponse("UP");
}