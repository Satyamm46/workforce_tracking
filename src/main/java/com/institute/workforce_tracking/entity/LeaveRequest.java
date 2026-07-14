package com.institute.workforce_tracking.entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.institute.workforce_tracking.enums.LeaveStatus;

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
 * An employee's request for a period of leave.
 *
 * <p>Carries the requested date range (inclusive on both ends), the reason,
 * the workflow status, and — once decided — who decided it and their optional
 * comment. The decision fields are null while the request is pending.</p>
 */
@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
public class LeaveRequest extends BaseEntity {

    /** The employee requesting leave. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** First day of leave (inclusive). */
    @Column(nullable = false)
    private LocalDate startDate;

    /** Last day of leave (inclusive). */
    @Column(nullable = false)
    private LocalDate endDate;

    /** The employee's stated reason. */
    @Column(nullable = false, length = 500)
    private String reason;

    /** Workflow state of this request. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveStatus status;

    /** The admin who approved or rejected; null while pending or cancelled. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by_user_id")
    private User decidedBy;

    /** Optional comment from the deciding admin. */
    @Column(length = 500)
    private String decisionComment;

    /**
     * Number of calendar days this request covers (both ends inclusive).
     * Derived from the range — not a column — so it can never disagree
     * with the stored dates.
     */
    public long getTotalDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
