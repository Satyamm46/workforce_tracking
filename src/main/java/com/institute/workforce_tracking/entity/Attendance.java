package com.institute.workforce_tracking.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.institute.workforce_tracking.enums.AttendanceStatus;

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
 * One employee's attendance record for one working day.
 *
 * <p>Created automatically on the user's first login of the day and completed
 * by an explicit clock-out. Working time is finalized at clock-out as
 * {@code (logout − login) − totalBreakMinutes} and stored, so history and
 * reports never recompute it.</p>
 *
 * <p>The pairing (user, workDate) is unique — one record per user per day,
 * enforced at the database level.</p>
 */
@Entity
@Table(
        name = "attendance",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attendance_user_work_date",
                columnNames = {"user_id", "work_date"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Attendance extends BaseEntity {

    /**
     * The employee this record belongs to. Many attendance rows relate to one
     * user. Loaded lazily — the user is fetched only when actually accessed.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The calendar day (in the institute's time zone) this record covers. */
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

     /** First login of the day; null for generated leave records. */
    private LocalDateTime loginTime;

    /** Clock-out time; null while the day is still in progress. */
    private LocalDateTime logoutTime;

    /**
     * Total break minutes accumulated during the day. Zero until the
     * break-management milestone starts populating it.
     */
    @Column(nullable = false)
    private int totalBreakMinutes = 0;

    /**
     * Final working minutes, computed and stored at clock-out.
     * Null while the day is still in progress.
     */
    private Integer workingMinutes;

    /** Current state of this working day. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    /**
     * True when the day was marked absent because no work report arrived
     * within the deadline. Reversible: worked minutes stay derivable from
     * loginTime/logoutTime/totalBreakMinutes, so a granted extension can
     * restore them.
     */
    @Column(nullable = false)
    @org.hibernate.annotations.ColumnDefault("false")
    private boolean absentNoReport = false;

    /**
     * True when the first check-in came more than the grace period (15 min)
     * after the start time the user declared in their work plan.
     */
    @Column(nullable = false)
    @org.hibernate.annotations.ColumnDefault("false")
    private boolean lateArrival = false;

    /**
     * True when the day counts as a half day — the penalty for a late
     * arrival. Reports credit only half the worked minutes for such days.
     */
    @Column(nullable = false)
    @org.hibernate.annotations.ColumnDefault("false")
    private boolean halfDay = false;
}