package com.institute.workforce_tracking.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.institute.workforce_tracking.enums.LectureStatus;

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
 * A scheduled teaching session.
 *
 * <p>Modeled as a date plus two times-of-day: a lecture never spans midnight,
 * so {@link LocalDate} + two {@link LocalTime}s expresses exactly what it is
 * and makes an invalid cross-day range unrepresentable.</p>
 *
 * <p>{@code extendedMinutes} is reserved for the tracking milestone's
 * extension feature (max 30 minutes) — zero until then.</p>
 */
@Entity
@Table(name = "lectures")
@Getter
@Setter
@NoArgsConstructor
public class Lecture extends BaseEntity {

    /** The teacher who scheduled and delivers this lecture. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_user_id", nullable = false)
    private User teacher;

    /** Subject being taught (e.g. "Mathematics"). */
    @Column(nullable = false, length = 100)
    private String subject;

    /** The class this lecture is for (e.g. "Grade 10"). */
    @Column(name = "class_name", nullable = false, length = 100)
    private String className;

    /** Optional batch within the class (e.g. "B"); null when not applicable. */
    @Column(length = 50)
    private String batch;

    /** The day the lecture takes place. */
    @Column(nullable = false)
    private LocalDate lectureDate;

    /** Scheduled start time of day. */
    @Column(nullable = false)
    private LocalTime startTime;

    /** Scheduled end time of day (before any extension). */
    @Column(nullable = false)
    private LocalTime endTime;

    /**
     * When the teacher actually started the class; null until started.
     * A late start shifts the whole session: the class keeps its planned
     * length, so the effective end is recalculated from this moment.
     */
    private LocalTime actualStartTime;

    /**
     * Whether the "starting soon" reminder (5 minutes before the scheduled
     * start) has been sent to the teacher. Prevents re-firing every tick.
     */
    @Column(nullable = false)
    private boolean startReminderSent = false;

    /**
     * Minutes added by extensions (tracking milestone; capped at 30).
     * The effective end of a lecture is endTime plus this value.
     */
    @Column(nullable = false)
    private int extendedMinutes = 0;

    /** Lifecycle state of this lecture. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LectureStatus status;
        
    /**
     * Whether the "ending soon" event has already been published for this
     * lecture. Prevents the reminder window from re-firing every scheduler
     * tick; reset when the lecture is extended so a fresh reminder goes out.
     */
    @Column(nullable = false)
    private boolean reminderSent = false;

    /**
     * When this lecture actually ends.
     *
     * <p>Once the teacher has started the class, the session keeps its
     * planned length but shifts to the actual start (a 1–2pm class started
     * at 1:15 ends at 2:15), plus any extensions. Before the class starts,
     * it is simply the scheduled end plus extensions. Derived — never
     * stored — so it cannot disagree with its inputs.</p>
     */
    public java.time.LocalTime getEffectiveEndTime() {
        long plannedMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        if (actualStartTime != null) {
            return actualStartTime.plusMinutes(plannedMinutes + extendedMinutes);
        }
        return endTime.plusMinutes(extendedMinutes);
    }


}
