package com.institute.workforce_tracking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.institute.workforce_tracking.entity.PasswordResetCode;

/**
 * Data-access layer for {@link PasswordResetCode} rows — one per email.
 */
@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {

    /** The current reset code row for an email, if one exists. */
    Optional<PasswordResetCode> findByEmail(String email);
}
