package com.institute.workforce_tracking.mapper;

import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.dto.request.CreateUserRequest;
import com.institute.workforce_tracking.dto.response.UserResponse;
import com.institute.workforce_tracking.entity.User;

/**
 * Converts between {@link User} entities and their DTO representations.
 *
 * <p>Centralizing this mapping in one place means the entity↔DTO translation
 * is defined once and reused by every service that needs it (auth, user
 * management, …) — no duplicated, drifting mapping logic.</p>
 */
@Component
public class UserMapper {

    /**
     * Maps a User entity to its safe, outbound representation.
     * The password is deliberately never copied.
     *
     * @param user the entity
     * @return the outbound DTO
     */
    public UserResponse toUserResponse(User user) {
    return new UserResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.isEnabled()
    );
}

    /**
     * Builds a new User entity from a create request.
     *
     * <p>The password must be pre-hashed by the caller — this mapper does no
     * encoding, keeping it a pure, dependency-free translation.</p>
     *
     * @param request         the create request
     * @param encodedPassword the already-hashed password
     * @return a new, unpersisted User entity (enabled by default)
     */
    public User toEntity(CreateUserRequest request, String encodedPassword) {
        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(encodedPassword);
        user.setRole(request.role());
        user.setEnabled(true);
        return user;
    }
}