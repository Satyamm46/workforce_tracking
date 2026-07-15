package com.institute.workforce_tracking.service;

import com.institute.workforce_tracking.dto.request.RegisterRequest;
import com.institute.workforce_tracking.dto.request.RegistrationDecisionRequest;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.dto.response.RegistrationResponse;
import com.institute.workforce_tracking.enums.RegistrationStatus;

/**
 * Business operations for the self-registration / approval workflow.
 *
 * <p>Only the Super Admin is created directly in the database (by the seeder).
 * Every other account starts as a registration request submitted through the
 * public form; a real {@code User} only comes into existence when the Super
 * Admin approves the request.</p>
 */
public interface RegistrationService {

    /**
     * Submits a new registration request (public, unauthenticated).
     *
     * @param request the applicant's details
     * @return the created request's safe representation
     * @throws com.institute.workforce_tracking.exception.DuplicateResourceException
     *         if the email already belongs to a user or a pending request
     * @throws com.institute.workforce_tracking.exception.BadRequestException
     *         if the requested role is SUPER_ADMIN
     */
    RegistrationResponse register(RegisterRequest request);

    /**
     * A page of registration requests in the given status, newest first.
     *
     * @param status the workflow status to filter by
     * @param page   zero-based page number
     * @param size   requested page size (clamped)
     * @return the matching requests plus paging metadata
     */
    PagedResponse<RegistrationResponse> getRequests(RegistrationStatus status, int page, int size);

    /** How many requests are currently pending. */
    long getPendingCount();

    /**
     * Approves a pending request: creates the user account (with the role
     * override if provided, otherwise the requested role) and marks the
     * request approved.
     *
     * @param id            the request id
     * @param decision      optional role override and comment
     * @param deciderEmail  the approving Super Admin's email
     * @return the decided request's safe representation
     */
    RegistrationResponse approve(Long id, RegistrationDecisionRequest decision, String deciderEmail);

    /**
     * Rejects a pending request. No account is created; the email may
     * register again later.
     *
     * @param id            the request id
     * @param decision      optional comment (e.g. rejection reason)
     * @param deciderEmail  the rejecting Super Admin's email
     * @return the decided request's safe representation
     */
    RegistrationResponse reject(Long id, RegistrationDecisionRequest decision, String deciderEmail);
}
