package com.institute.workforce_tracking.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.request.LoginRequest;
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

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UserRepository userRepository,
                           UserMapper userMapper,
                           ApplicationEventPublisher eventPublisher) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
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
}