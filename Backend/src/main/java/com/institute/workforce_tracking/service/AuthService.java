package com.institute.workforce_tracking.service;

import com.institute.workforce_tracking.dto.request.LoginRequest;
import com.institute.workforce_tracking.dto.response.AuthResponse;
import com.institute.workforce_tracking.dto.response.UserResponse;

/**
 * Business operations for authentication.
 *
 * <p>Defined as an interface so the controller depends on an abstraction, not
 * a concrete class (Dependency Inversion Principle). The implementation can be
 * swapped or mocked in tests without touching any caller.</p>
 */
public interface AuthService {

    /**
     * Authenticates a user's credentials and issues a JWT.
     *
     * @param request the login credentials
     * @return the issued token plus the authenticated user's safe details
     */
    AuthResponse login(LoginRequest request);

    /**
     * Loads the safe details of the currently authenticated user.
     *
     * @param email the authenticated user's email (from the security context)
     * @return the user's safe representation
     */
    UserResponse getCurrentUser(String email);
}