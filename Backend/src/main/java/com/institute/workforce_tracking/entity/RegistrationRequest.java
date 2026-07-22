package com.institute.workforce_tracking.entity;

import com.institute.workforce_tracking.enums.RegistrationStatus;
import com.institute.workforce_tracking.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A self-registration request awaiting Super Admin approval.
 *
 * <p>Only the Super Admin is seeded directly into the database. Everyone else
 * (employee, teacher, admin) signs up through the public registration form,
 * which creates one of these rows. No {@link User} account exists until the
 * Super Admin approves the request — at which point the stored details
 * (including the already-hashed password) are copied into a new User.</p>
 */
@Entity
@Table(name = "registration_requests")
@Getter
@Setter
@NoArgsConstructor
public class RegistrationRequest extends BaseEntity {

    /** The applicant's display name. */
    @Column(nullable = false, length = 100)
    private String fullName;

    /**
     * The applicant's email — becomes the login identifier once approved.
     * Not unique at the DB level: a rejected email may apply again, leaving
     * multiple historical rows. Uniqueness among PENDING rows and against
     * existing users is enforced in the service layer.
     */
    @Column(nullable = false, length = 150)
    private String email;

    /** BCrypt-hashed password chosen at signup; copied to the User on approval. */
    @Column(nullable = false)
    private String password;

    /** Optional contact number (international format, no '+'), copied on approval. */
    @Column(length = 20)
    private String phone;

    /**
     * The role the applicant asked for. The Super Admin may assign a different
     * role when approving; this field records what was requested.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role requestedRole;

    /** Workflow state of this request. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status;

    /** The Super Admin who decided; null while pending. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by_user_id")
    private User decidedBy;

    /** Optional comment from the deciding Super Admin (e.g. rejection reason). */
    @Column(length = 500)
    private String decisionComment;
}
