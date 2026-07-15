package com.institute.workforce_tracking.controller;

import com.institute.workforce_tracking.constants.ApiConstants;
import com.institute.workforce_tracking.dto.response.HealthResponse;
import com.institute.workforce_tracking.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes a lightweight liveness endpoint for the application.
 *
 * <p>Reached at {@code GET /api/health} (the {@code /api} prefix comes from
 * the server context-path). Unlike the deep Actuator health endpoint, this is
 * a simple, contract-defined "is the app responding?" ping that always returns
 * the standard {@link ApiResponse} envelope.</p>
 *
 * <p>This controller is intentionally trivial: a controller's job is to handle
 * the HTTP concern and delegate. Here there is no business logic to delegate,
 * so it simply returns a fixed status. Future controllers will inject a service
 * and delegate to it — never contain business logic themselves.</p>
 */
@RestController
public class HealthController {

    /**
     * Reports that the application is up and serving requests.
     *
     * @return HTTP 200 with the standard envelope wrapping {@link HealthResponse}
     */
    @GetMapping(ApiConstants.HEALTH)
    public ResponseEntity<ApiResponse<HealthResponse>> health() {
        ApiResponse<HealthResponse> body =
                ApiResponse.of("Service is healthy", HealthResponse.UP);
        return ResponseEntity.ok(body);
    }
}