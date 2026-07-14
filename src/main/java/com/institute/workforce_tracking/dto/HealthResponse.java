package com.institute.workforce_tracking.dto;

/**
 * Payload describing the application's liveness status.
 *
 * @param status a short liveness indicator, e.g. "UP"
 */
public record HealthResponse(String status) {

    /** Canonical "service is up" instance, reused to avoid re-allocation. */
    public static final HealthResponse UP = new HealthResponse("UP");
}
