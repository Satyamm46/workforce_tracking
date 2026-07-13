package com.institute.workforce_tracking.entity;

import com.institute.workforce_tracking.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A user account — the authentication and authorization identity in the system.
 *
 * <p>Holds login credentials, the user's role, and basic identity. Attendance,
 * scheduling, and other domain data are handled by later milestones; this
 * entity is intentionally focused on "who can log in and what may they do".</p>
 *
 * <p>Inherits {@code id}, {@code createdAt}, and {@code updatedAt} from
 * {@link BaseEntity}.</p>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    /** The user's display name. */
    @Column(nullable = false, length = 100)
    private String fullName;

    /**
     * Email address, used as the unique login identifier.
     * Enforced unique at the database level.
     */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * BCrypt-hashed password. NEVER stored in plain text and never returned
     * in any API response.
     */
    @Column(nullable = false)
    private String password;

    /**
     * The user's single role, persisted as its String name (e.g. "ADMIN")
     * rather than its ordinal, for stability and readability.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * Whether the account is active. A disabled account cannot authenticate.
     * Defaults to true for newly created users.
     */
    @Column(nullable = false)
    private boolean enabled = true;
}