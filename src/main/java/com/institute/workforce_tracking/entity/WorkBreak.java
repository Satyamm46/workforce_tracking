package com.institute.workforce_tracking.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One break taken during a working day.
 *
 * <p>An attendance record can have many breaks. A break with a null
 * {@code endTime} is "open" — the employee is currently on it; at most one
 * open break can exist per attendance record, enforced by the service's
 * state-transition guards (a break can only start from WORKING status).</p>
 *
 * <p>{@code durationMinutes} is computed and stored when the break ends, and
 * simultaneously accumulated onto the parent attendance's
 * {@code totalBreakMinutes}.</p>
 */
@Entity
@Table(name = "work_breaks")
@Getter
@Setter
@NoArgsConstructor
public class WorkBreak extends BaseEntity {

    /** The working day this break belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    /** When the break started. */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /** When the break ended; null while the break is ongoing. */
    private LocalDateTime endTime;

    /** Break length in minutes, computed and stored when the break ends. */
    private Integer durationMinutes;
}