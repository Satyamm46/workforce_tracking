package com.institute.workforce_tracking.service.impl;

import com.institute.workforce_tracking.dto.request.LoginRequest;
import com.institute.workforce_tracking.dto.response.AuthResponse;
import com.institute.workforce_tracking.dto.response.UserResponse;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.exception.InvalidCredentialsException;
import com.institute.workforce_tracking.exception.ResourceNotFoundException;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.security.JwtUtil;
import com.institute.workforce_tracking.security.SecurityUser;
import com.institute.workforce_tracking.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link AuthService}.
 *
 * <p>Delegates credential verification to Spring Security's
 * {@link AuthenticationManager}, then mints a JWT via {@link JwtUtil}. Business
 * logic lives here — never in the controller.</p>
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            // Delegates to the DaoAuthenticationProvider -> CustomUserDetailsService
            // -> PasswordEncoder. Throws AuthenticationException on any failure.
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException ex) {
            // Translate Spring Security's failure into our domain 401, with a
            // generic message that does not reveal which part failed.
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Authentication succeeded: the principal is our SecurityUser adapter.
        SecurityUser principal = (SecurityUser) authentication.getPrincipal();
        User user = principal.getDomainUser();

        String token = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());

        return AuthResponse.of(token, toUserResponse(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return toUserResponse(user);
    }

    /**
     * Maps a domain User to its safe, outbound representation.
     * Kept private for now (single consumer); promote to a dedicated
     * UserMapper in the {@code mapper} package when a second service needs it.
     */
    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}