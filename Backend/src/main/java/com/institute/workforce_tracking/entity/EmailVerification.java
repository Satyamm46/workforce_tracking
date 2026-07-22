package com.institute.workforce_tracking.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A one-time email verification code issued during self-registration.
 *
 * <p>Proves the applicant controls the email address before any registration
 * request is accepted: a code is emailed, and registration only proceeds when
 * the same code is presented back. This blocks sign-ups against addresses the
 * applicant does not own.</p>
 *
 * <p>The code itself is never stored in plaintext — only a BCrypt hash, the
 * same way passwords are handled. One row per email (unique constraint): a
 * fresh request overwrites the previous code rather than accumulating rows.</p>
 */
@Entity
@Table(
        name = "email_verifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_email_verification_email",
                columnNames = "email"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class EmailVerification extends BaseEntity {

    /** The email address being verified (lower-cased for stable matching). */
    @Column(nullable = false, length = 150)
    private String email;

    /** BCrypt hash of the 6-digit code; never the code itself. */
    @Column(nullable = false)
    private String codeHash;

    /** When the code stops being valid (issue time + the code's lifetime). */
    @Column(nullable = false)
    private Instant expiresAt;

    /**
     * How many times a code has been checked against this row. A cap prevents
     * brute-forcing the 6-digit space; exceeding it forces a new code.
     */
    @Column(nullable = false)
    private int attempts = 0;

    /**
     * True once the code has been used to complete a registration. A consumed
     * row can never verify again, so a submitted code cannot be replayed.
     */
    @Column(nullable = false)
    private boolean consumed = false;
}
