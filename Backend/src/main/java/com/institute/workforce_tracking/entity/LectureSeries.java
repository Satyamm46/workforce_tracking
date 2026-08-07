package com.institute.workforce_tracking.entity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.annotations.ColumnDefault;

import com.institute.workforce_tracking.enums.RecurrenceFrequency;

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
 * A repeating teaching commitment — the template a teacher defines once
 * ("Maths for Grade 10, Mon/Wed/Fri, 10:00–11:00") from which individual
 * {@link Lecture} rows are generated.
 *
 * <p>A series is a <em>template</em>, never a lecture. Everything downstream of
 * a lecture — the LIVE/COMPLETED/MISSED sweeps, summaries, conflict detection,
 * teaching reports — works on concrete {@code Lecture} rows, so occurrences are
 * materialised into real rows rather than resolved at read time. Nothing
 * downstream has to learn about recurrence.</p>
 *
 * <p>Occurrences are created on a rolling horizon rather than all at once,
 * because a series may be open-ended. {@link #materializedThrough} is the
 * watermark that makes the nightly top-up incremental and idempotent.</p>
 */
@Entity
@Table(name = "lecture_series")
@Getter
@Setter
@NoArgsConstructor
public class LectureSeries extends BaseEntity {

    /** The teacher who owns this series and delivers every occurrence. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_user_id", nullable = false)
    private User teacher;

    /** Subject taught by every occurrence (e.g. "Mathematics"). */
    @Column(nullable = false, length = 100)
    private String subject;

    /** The class every occurrence is for (e.g. "Grade 10"). */
    @Column(name = "class_name", nullable = false, length = 100)
    private String className;

    /** Optional batch within the class; null when not applicable. */
    @Column(length = 50)
    private String batch;

    /** Start time of day, identical for every occurrence. */
    @Column(nullable = false)
    private LocalTime startTime;

    /** End time of day, identical for every occurrence. */
    @Column(nullable = false)
    private LocalTime endTime;

    /** Whether this series repeats weekly or monthly. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecurrenceFrequency frequency;

    /**
     * The weekdays a WEEKLY series lands on, stored as a comma-joined list of
     * {@link DayOfWeek} names ("MONDAY,WEDNESDAY,FRIDAY"); null for MONTHLY.
     *
     * <p>A {@code @ElementCollection} side table would be more orthodox, but
     * this project has no migration tooling — every table is a hand-written
     * production DDL step — so one column on an existing table beats one more
     * table. Read and written through {@link #getWeekdays()} /
     * {@link #setWeekdays(Set)}; the raw accessors exist only for JPA.</p>
     */
    @Column(name = "days_of_week", length = 80)
    private String daysOfWeek;

    /**
     * The day of the month a MONTHLY series lands on (1–31); null for WEEKLY.
     * Clamped to the month's length at generation time, so a series on the
     * 31st runs on the 30th — or the 28th — in shorter months.
     */
    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    /** First day the series is eligible to produce an occurrence. */
    @Column(nullable = false)
    private LocalDate startDate;

    /** Last eligible day; null means the series is open-ended. */
    private LocalDate endDate;

    /**
     * Whether this series still produces occurrences. Stopping a series clears
     * this instead of deleting the row, so past occurrences keep a parent and
     * reports stay honest.
     */
    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean active = true;

    /**
     * The last date occurrences have been generated through; null before the
     * first run. The nightly top-up resumes from the day after this, which is
     * what keeps repeated sweeps from creating duplicates.
     */
    @Column(name = "materialized_through")
    private LocalDate materializedThrough;

    /**
     * The weekdays this series lands on, as a set. Empty for a MONTHLY series
     * or a WEEKLY one with nothing selected (which the service rejects).
     */
    public Set<DayOfWeek> getWeekdays() {
        if (daysOfWeek == null || daysOfWeek.isBlank()) {
            return EnumSet.noneOf(DayOfWeek.class);
        }
        return Arrays.stream(daysOfWeek.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DayOfWeek.class)));
    }

    /**
     * Stores the weekdays this series lands on. Written in calendar order
     * (Monday first) rather than selection order, so the column reads the same
     * way regardless of how the teacher ticked the boxes.
     */
    public void setWeekdays(Set<DayOfWeek> weekdays) {
        if (weekdays == null || weekdays.isEmpty()) {
            this.daysOfWeek = null;
            return;
        }
        this.daysOfWeek = EnumSet.copyOf(weekdays).stream()
                .map(DayOfWeek::name)
                .collect(Collectors.joining(","));
    }
}
