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

    /** Base path for notification endpoints (reached at /api/v1/notifications). */
    public static final String NOTIFICATIONS_BASE = API_VERSION_V1 + "/notifications";

    public static final String REPORTS_BASE = API_VERSION_V1 + "/reports";

    /** Base path for dashboard endpoints (reached at /api/v1/dashboard). */
    public static final String DASHBOARD_BASE = API_VERSION_V1 + "/dashboard";

    /** Base path for self-registration endpoints (reached at /api/v1/registrations). */
    public static final String REGISTRATIONS_BASE = API_VERSION_V1 + "/registrations";

    /** Base path for push-subscription endpoints (reached at /api/v1/push). */
    public static final String PUSH_BASE = API_VERSION_V1 + "/push";

    /** Base path for work-plan endpoints (reached at /api/v1/work-plans). */
    public static final String WORK_PLANS_BASE = API_VERSION_V1 + "/work-plans";

    /** Base path for work-report endpoints (reached at /api/v1/work-reports). */
    public static final String WORK_REPORTS_BASE = API_VERSION_V1 + "/work-reports";

    /** Base path for lecture-summary endpoints (reached at /api/v1/lecture-summaries). */
    public static final String LECTURE_SUMMARIES_BASE = API_VERSION_V1 + "/lecture-summaries";

    /** Base path for deadline-extension endpoints (reached at /api/v1/deadline-extensions). */
    public static final String DEADLINE_EXTENSIONS_BASE = API_VERSION_V1 + "/deadline-extensions";
}
