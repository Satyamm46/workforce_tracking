package com.institute.workforce_tracking.constants;

public final class ApiConstants {

    private ApiConstants() {
        // Prevent instantiation — this class only holds constants.
    }

    /** API version segment. The /api context-path is applied by the server. */
    public static final String API_VERSION_V1 = "/v1";

    /** Public health-check endpoint (reached at /api/health). */
    public static final String HEALTH = "/health";

    /** Base path for authentication endpoints (reached at /api/v1/auth). */
    public static final String AUTH_BASE = API_VERSION_V1 + "/auth";
}