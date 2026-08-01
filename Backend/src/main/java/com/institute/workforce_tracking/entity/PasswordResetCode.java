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
 * A one-time code issued so a user who has forgotten their password can set a
 * new one.
 *
 * <p>The same shape as {@link EmailVerification} — hashed code, expiry, attempt
 * cap, single-use — but a separate table on purpose: a registration code and a
 * reset code can be outstanding for the same address at the same time, and the
 * two must never overwrite or consume one another.</p>
 *
 * <p>The code is stored only as a BCrypt hash, the same way passwords are. One
 * row per email (unique constraint): requesting a fresh code overwrites the
 * previous one rather than accumulating rows, so an old code stops working the
 * moment a new one is issued.</p>
 */
@Entity
@Table(
        name = "password_reset_codes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_password_reset_email",
                columnNames = "email"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetCode extends BaseEntity {

    /** The account's email address (lower-cased for stable matching). */
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
     * True once the code has been used to change a password. A consumed row can
     * never verify again, so a code cannot be replayed to reset the password a
     * second time.
     */
    @Column(nullable = false)
    private boolean consumed = false;
}
