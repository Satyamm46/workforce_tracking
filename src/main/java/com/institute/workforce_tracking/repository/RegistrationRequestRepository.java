package com.institute.workforce_tracking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.institute.workforce_tracking.entity.RegistrationRequest;
import com.institute.workforce_tracking.enums.RegistrationStatus;

/**
 * Data-access layer for {@link RegistrationRequest} entities.
 */
@Repository
public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {

    /**
     * Whether the given email already has a request in the given status.
     * Used to block a second PENDING request for the same email.
     */
    boolean existsByEmailAndStatus(String email, RegistrationStatus status);

    /** A page of requests in the given status, for the admin review screen. */
    Page<RegistrationRequest> findByStatus(RegistrationStatus status, Pageable pageable);

    /** How many requests are pending (drives the admin badge). */
    long countByStatus(RegistrationStatus status);
}
