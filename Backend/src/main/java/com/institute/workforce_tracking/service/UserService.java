package com.institute.workforce_tracking.service;

import com.institute.workforce_tracking.dto.request.CreateUserRequest;
import com.institute.workforce_tracking.dto.request.UpdateUserRequest;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.dto.response.UserResponse;

/**
 * Business operations for managing user accounts (employees and teachers).
 *
 * <p>All methods operate in terms of DTOs, never entities — the entity never
 * crosses this service boundary. Implementations enforce business rules such
 * as email uniqueness and translate persistence outcomes into the application's
 * domain exceptions.</p>
 */
public interface UserService {

    /**
     * Creates a new user with a hashed password.
     *
     * @param request the new user's details
     * @return the created user's safe representation
     * @throws com.institute.workforce_tracking.exception.DuplicateResourceException
     *         if the email is already in use
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Returns a page of users.
     *
     * @param page zero-based page number
     * @param size requested page size (clamped to a safe maximum)
     * @return a page of user representations plus paging metadata
     */
    PagedResponse<UserResponse> getAllUsers(int page, int size);

    /**
     * Returns a single user by id.
     *
     * @param id the user id
     * @return the user's safe representation
     * @throws com.institute.workforce_tracking.exception.ResourceNotFoundException
     *         if no user has that id
     */
    UserResponse getUserById(Long id);

    /**
     * Updates a user's editable details (name and role).
     *
     * @param id      the user id
     * @param request the updated details
     * @return the updated user's safe representation
     * @throws com.institute.workforce_tracking.exception.ResourceNotFoundException
     *         if no user has that id
     */
    UserResponse updateUser(Long id, UpdateUserRequest request);

    /**
     * Deactivates a user (soft delete) — they can no longer authenticate,
     * but their record and history are preserved.
     *
     * @param id the user id
     * @throws com.institute.workforce_tracking.exception.ResourceNotFoundException
     *         if no user has that id
     */
    void deactivateUser(Long id);

    /**
     * Reactivates a previously deactivated user.
     *
     * @param id the user id
     * @throws com.institute.workforce_tracking.exception.ResourceNotFoundException
     *         if no user has that id
     */
    void activateUser(Long id);
}