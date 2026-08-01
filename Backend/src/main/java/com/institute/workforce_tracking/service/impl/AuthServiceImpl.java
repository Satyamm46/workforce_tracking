package com.institute.workforce_tracking.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.request.LoginRequest;
import com.institute.workforce_tracking.dto.request.ResetPasswordRequest;
import com.institute.workforce_tracking.dto.response.AuthResponse;
import com.institute.workforce_tracking.dto.response.UserResponse;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.event.UserLoggedInEvent;
import com.institute.workforce_tracking.exception.InvalidCredentialsException;
import com.institute.workforce_tracking.exception.ResourceNotFoundException;
import com.institute.workforce_tracking.mapper.UserMapper;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.security.JwtUtil;
import com.institute.workforce_tracking.security.SecurityUser;
import com.institute.workforce_tracking.service.AuthService;
import com.institute.workforce_tracking.service.PasswordResetService;

import com.institute.workforce_tracking.util.EmailRateLimiter;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailRateLimiter rateLimiter;
    private final PasswordResetService passwordResetService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UserRepository userRepository,
                           UserMapper userMapper,
                           ApplicationEventPublisher eventPublisher,
                           EmailRateLimiter rateLimiter,
                           PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
        this.rateLimiter = rateLimiter;
        this.passwordResetService = passwordResetService;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        rateLimiter.check(request.email());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        SecurityUser principal = (SecurityUser) authentication.getPrincipal();
        User user = principal.getDomainUser();

        String token = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());

        // Announce the successful login. Auth knows nothing about who listens.
        // (No listener today — attendance is started manually via Check In —
        // but the event stays for future consumers, e.g. audit logging.)
        eventPublisher.publishEvent(new UserLoggedInEvent(
                user.getId(), user.getEmail(), user.getFullName(), user.getRole()));

        return AuthResponse.of(token, userMapper.toUserResponse(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toUserResponse(user);
    }

    @Override
    public void forgotPassword(String email) {
        // Rate-limited like login: this endpoint is public, sends mail, and is
        // otherwise an easy way to drain the SMTP quota or spam an inbox.
        rateLimiter.check(email);
        passwordResetService.sendCode(email);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        // The code is only 6 digits; without a limit here the per-code attempt
        // cap could be sidestepped by requesting fresh codes in a loop.
        rateLimiter.check(request.email());
        passwordResetService.resetPassword(
                request.email(), request.otp(), request.newPassword());
    }
}