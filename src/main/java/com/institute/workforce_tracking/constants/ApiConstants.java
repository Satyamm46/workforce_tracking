package com.institute.workforce_tracking.constants;

/**
 * Constants describing the API's URL structure.
 *
 * <p>Kept separate from {@link AppConstants} because these describe the
 * <em>web contract</em> (paths, versioning), a different concern from general
 * application values. Controllers reference these instead of hard-coding
 * string literals, so route changes happen in one place.</p>
 *
 * <p>Non-instantiable holder of static constants.</p>
 */
public final class ApiConstants {

    private ApiConstants() {
        // Prevent instantiation — this class only holds constants.
    }

    /**
     * API version segment. NOTE: the global context-path {@code /api} is
     * applied by the server (see {@code server.servlet.context-path}), so
     * these paths do NOT repeat {@code /api}. A controller mapped to
     * {@code HEALTH} is reached at {@code /api/health}.
     */
    public static final String API_VERSION_V1 = "/v1";

    /** Public health-check endpoint (reached at {@code /api/health}). */
    public static final String HEALTH = "/health";
}