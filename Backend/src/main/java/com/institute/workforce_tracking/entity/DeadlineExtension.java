package com.institute.workforce_tracking.entity;

import java.time.LocalDate;

import com.institute.workforce_tracking.enums.DeadlineType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An admin-granted grace period on one user's submission deadline for one
 * day: work plan, work report, or lecture summary.
 *
 * <p>The penalty sweeps consult this table before punishing a missed
 * deadline, adding {@code extraHours} to the normal window. Granting an
 * extension also reverses an already-applied penalty for that day.</p>
 */
@Entity
@Table(name = "deadline_extensions",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "type", "target_date"}))
@Getter
@Setter
@NoArgsConstructor
public class DeadlineExtension extends BaseEntity {

    /** The user whose deadline is being extended. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Which deadline this extension applies to. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeadlineType type;

    /**
     * The day the deadline belongs to: the work date (report), the lecture
     * date (summary), or the plan date (work plan).
     */
    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    /** Hours added to the normal deadline window (48 by default). */
    @Column(nullable = false)
    private int extraHours = 48;

    /** Email of the admin who granted this extension. */
    @Column(nullable = false, length = 150)
    private String grantedBy;

    /** Optional reason shown to the user and in the admin list. */
    @Column(length = 500)
    private String reason;
}
