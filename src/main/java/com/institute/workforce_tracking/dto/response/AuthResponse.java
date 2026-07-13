package com.institute.workforce_tracking.dto.response;

/**
 * Outbound payload returned on successful login.
 *
 * <p>Bundles the issued JWT with the token type and the authenticated user's
 * safe details, so the frontend can store the token and immediately render the
 * user's identity without a second request.</p>
 *
 * @param accessToken the signed JWT to send on subsequent requests
 * @param tokenType   the auth scheme; always "Bearer"
 * @param user        the authenticated user's safe details
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        UserResponse user
) {

    /** Standard token type for the Authorization header. */
    private static final String BEARER = "Bearer";

    /**
     * Convenience factory that fixes the token type to "Bearer".
     *
     * @param accessToken the signed JWT
     * @param user        the authenticated user's safe details
     * @return an AuthResponse with tokenType "Bearer"
     */
    public static AuthResponse of(String accessToken, UserResponse user) {
        return new AuthResponse(accessToken, BEARER, user);
    }
}