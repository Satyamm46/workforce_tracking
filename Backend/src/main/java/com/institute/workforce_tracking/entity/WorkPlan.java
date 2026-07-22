package com.institute.workforce_tracking.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One user's declared plan for a working day, submitted the day before:
 * when they intend to start and finish, and what they will work on.
 *
 * <p>Admins and Employees must have a plan for a day before they can check
 * in on it. A plan submitted on the planned day itself (to unblock a
 * forgotten submission) is marked {@code submittedLate}.</p>
 */
@Entity
@Table(name = "work_plans",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "plan_date"}))
@Getter
@Setter
@NoArgsConstructor
public class WorkPlan extends BaseEntity {

    /** The user this plan belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The day being planned. */
    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    /** Intended start-of-work time. */
    @Column(nullable = false)
    private LocalTime plannedStartTime;

    /** Intended end-of-work time. */
    @Column(nullable = false)
    private LocalTime plannedEndTime;

    /** What the user intends to work on. */
    @Column(nullable = false, length = 1000)
    private String workDescription;

    /** True when submitted on the planned day itself rather than the day before. */
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean submittedLate = false;

    /**
     * Whether the "your work day is about to start" reminder has been sent for
     * this plan. Prevents the per-minute sweep from re-firing it every tick.
     */
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean startReminderSent = false;
}
