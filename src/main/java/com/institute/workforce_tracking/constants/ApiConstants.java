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

    /** Base path for user management endpoints (reached at /api/v1/users). */
    public static final String USERS_BASE = API_VERSION_V1 + "/users";

    /** Base path for attendance endpoints (reached at /api/v1/attendance). */
    public static final String ATTENDANCE_BASE = API_VERSION_V1 + "/attendance";

    /** Base path for leave endpoints (reached at /api/v1/leaves). */
    public static final String LEAVES_BASE = API_VERSION_V1 + "/leaves";

    /** Base path for lecture endpoints (reached at /api/v1/lectures). */
    public static final String LECTURES_BASE = API_VERSION_V1 + "/lectures";
}
