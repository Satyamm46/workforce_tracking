package com.institute.workforce_tracking.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.constants.AppConstants;
import com.institute.workforce_tracking.dto.request.CreateUserRequest;
import com.institute.workforce_tracking.dto.request.UpdateUserRequest;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.dto.response.UserResponse;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.exception.DuplicateResourceException;
import com.institute.workforce_tracking.exception.ResourceNotFoundException;
import com.institute.workforce_tracking.mapper.UserMapper;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.service.UserService;

/**
 * Default implementation of {@link UserService}.
 *
 * <p>Owns the business rules for user management: email uniqueness, password
 * hashing, safe pagination limits, and soft deletion. All entity access is via
 * {@link UserRepository}; all outbound data is mapped to DTOs so entities never
 * leave this layer.</p>
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = userMapper.toEntity(request, encodedPassword);
        User saved = userRepository.save(user);

        return userMapper.toUserResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = normalizePageSize(size);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<UserResponse> result = userRepository.findAll(pageable)
                .map(userMapper::toUserResponse);

        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userMapper.toUserResponse(getUserEntity(id));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = getUserEntity(id);
        user.setFullName(request.fullName());
        user.setRole(request.role());
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        User user = getUserEntity(id);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activateUser(Long id) {
        User user = getUserEntity(id);
        user.setEnabled(true);
        userRepository.save(user);
    }

    /**
     * Loads a user or throws a 404-mapped exception if absent.
     */
    private User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * Clamps a requested page size to a safe range, protecting the database
     * from oversized requests.
     */
    private int normalizePageSize(int size) {
        if (size <= 0) {
            return AppConstants.DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, AppConstants.MAX_PAGE_SIZE);
    }
}