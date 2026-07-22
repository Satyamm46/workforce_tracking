package com.institute.workforce_tracking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.institute.workforce_tracking.entity.EmailVerification;

/**
 * Data-access layer for {@link EmailVerification} rows — one per email.
 */
@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    /** The current verification row for an email, if one exists. */
    Optional<EmailVerification> findByEmail(String email);
}
