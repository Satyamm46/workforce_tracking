package com.institute.workforce_tracking.controller;

import com.institute.workforce_tracking.constants.ApiConstants;
import com.institute.workforce_tracking.dto.request.ForgotPasswordRequest;
import com.institute.workforce_tracking.dto.request.LoginRequest;
import com.institute.workforce_tracking.dto.request.ResetPasswordRequest;
import com.institute.workforce_tracking.dto.response.AuthResponse;
import com.institute.workforce_tracking.dto.response.UserResponse;
import com.institute.workforce_tracking.dto.response.ApiResponse;
import com.institute.workforce_tracking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for authentication.
 *
 * <p>Deliberately thin: each method validates/receives input, delegates to
 * {@link AuthService}, and wraps the result in the standard {@link ApiResponse}
 * envelope. No business logic lives here.</p>
 */
@RestController
@RequestMapping(ApiConstants.AUTH_BASE)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates a user and returns a JWT.
     *
     * @param request the validated login credentials
     * @return 200 with the token and the authenticated user's details
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.of("Login successful", authResponse));
    }

    /**
     * Returns the currently authenticated user's details.
     *
     * <p>Reachable only with a valid JWT (enforced by the security filter
     * chain). The {@link Authentication} is injected by Spring Security and its
     * name is the authenticated user's email.</p>
     *
     * @param authentication the current security context's authentication
     * @return 200 with the current user's safe details
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            Authentication authentication) {

        UserResponse user = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.of("Current user retrieved", user));
    }

    /**
     * Public: emails a one-time code that lets the holder set a new password.
     *
     * <p>An address with no account is rejected (404) rather than silently
     * accepted, so someone who mistypes their email is told so instead of
     * waiting on a code that was never sent.</p>
     *
     * @param request the address to send the code to
     * @return 200 once the code is on its way
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request.email());
        return ResponseEntity.ok(ApiResponse.of(
                "A reset code has been sent to your email.", null));
    }

    /**
     * Public: sets a new password for the account, authorized by the code that
     * was emailed rather than by the forgotten password.
     *
     * @param request the email, the 6-digit code, and the new password
     * @return 200 once the password has been changed
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.of(
                "Password updated. You can now sign in with your new password.", null));
    }
}