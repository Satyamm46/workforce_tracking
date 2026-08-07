package com.institute.workforce_tracking.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.institute.workforce_tracking.entity.Lecture;
import com.institute.workforce_tracking.entity.LectureSeries;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.enums.LectureStatus;

/**
 * Data-access layer for {@link Lecture} records.
 */
@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {

    /**
     * Does the teacher already have an active (scheduled or live) lecture on
     * this date whose time range overlaps the given one?
     *
     * <p>Overlap uses STRICT inequalities ({@code s1 < e2 AND e1 > s2}):
     * back-to-back lectures — one ending exactly when the next begins — do
     * NOT conflict. Cancelled and completed lectures never conflict.</p>
     *
     * @param teacher     the teacher
     * @param lectureDate the day being scheduled
     * @param startTime   proposed start
     * @param endTime     proposed end
     * @return true if a conflicting lecture exists
     */
    @Query("""
            SELECT COUNT(l) > 0
            FROM Lecture l
            WHERE l.teacher = :teacher
              AND l.lectureDate = :lectureDate
              AND l.status IN (com.institute.workforce_tracking.enums.LectureStatus.SCHEDULED,
                               com.institute.workforce_tracking.enums.LectureStatus.LIVE)
              AND l.startTime < :endTime
              AND l.endTime > :startTime
            """)
    boolean existsConflictingLecture(@Param("teacher") User teacher,
                                     @Param("lectureDate") LocalDate lectureDate,
                                     @Param("startTime") LocalTime startTime,
                                     @Param("endTime") LocalTime endTime);

    /**
     * A page of one teacher's lectures from a given date onward — their
     * upcoming schedule. Sorting (by date, then start time) comes from the
     * Pageable supplied by the service.
     *
     * @param teacher  the teacher
     * @param fromDate include lectures on or after this day
     * @param pageable page, size, and sort
     * @return a page of the teacher's upcoming lectures
     */
    Page<Lecture> findByTeacherAndLectureDateGreaterThanEqual(
            User teacher, LocalDate fromDate, Pageable pageable);

    /**
     * Admin view: a page of all lectures on one day, with each lecture's
     * teacher pre-fetched for display.
     */
    @EntityGraph(attributePaths = "teacher")
    Page<Lecture> findByLectureDate(LocalDate lectureDate, Pageable pageable);

        /**
     * All lectures in a given status, for the completion and reminder sweeps.
     *
     * <p>Deliberately unfiltered by date: the completion sweep must also catch
     * stale LIVE lectures from previous days (e.g. after a server outage).
     * The set of concurrently live lectures is small by nature, so loading and
     * filtering in Java keeps the effective-end arithmetic
     * (endTime + extendedMinutes) in one place instead of duplicating it in
     * JPQL.</p>
     */
    List<Lecture> findByStatus(LectureStatus status);

    /** How many lectures are currently in the given status. */
    long countByStatus(LectureStatus status);

    /**
     * One teacher's lectures on a day in a given status. Backs the deadline
     * extension's penalty reversal (SUMMARY_MISSED → COMPLETED).
     */
    List<Lecture> findByTeacherAndLectureDateAndStatus(
            User teacher, LocalDate lectureDate, LectureStatus status);

        /**
     * All completed lectures in a date range, with teachers pre-fetched.
     * The report service aggregates these in Java, because teaching minutes
     * require LocalTime arithmetic that belongs in the domain layer.
     */
    @EntityGraph(attributePaths = "teacher")
    List<Lecture> findByStatusAndLectureDateBetween(
            LectureStatus status, LocalDate startDate, LocalDate endDate);

    /**
     * One teacher's lectures across a date range, for their calendar month.
     * Unpaged on purpose: a bounded range (the service caps it) of one
     * teacher's classes is a small, whole-month result the grid renders at once.
     */
    List<Lecture> findByTeacherAndLectureDateBetweenOrderByLectureDateAscStartTimeAsc(
            User teacher, LocalDate fromDate, LocalDate toDate);

    /** Every teacher's lectures across a date range, for the admin calendar. */
    @EntityGraph(attributePaths = "teacher")
    List<Lecture> findByLectureDateBetweenOrderByLectureDateAscStartTimeAsc(
            LocalDate fromDate, LocalDate toDate);

    /**
     * Lectures on one day, in one status, still awaiting the day-before
     * digest. The flag makes the evening sweep idempotent, so a later tick
     * only picks up classes scheduled after the first one ran.
     */
    @EntityGraph(attributePaths = "teacher")
    List<Lecture> findByLectureDateAndStatusAndDayBeforeReminderSentFalse(
            LocalDate lectureDate, LectureStatus status);

    /**
     * A series' occurrences from a date onward in a given status. Backs
     * stopping a series: future occurrences are cancelled, past ones are left
     * alone so reports stay honest.
     */
    List<Lecture> findBySeriesAndLectureDateGreaterThanEqualAndStatus(
            LectureSeries series, LocalDate fromDate, LectureStatus status);

    /**
     * All lectures in a status on one specific day — the bounded form of
     * {@link #findByStatus(LectureStatus)} for sweeps that only ever care
     * about today. Materialised series push the SCHEDULED row count weeks into
     * the future, so the per-minute sweeps must not scan all of it.
     */
    List<Lecture> findByStatusAndLectureDate(LectureStatus status, LocalDate lectureDate);

    /** All lectures in a status on or before a day, for the missed sweep. */
    List<Lecture> findByStatusAndLectureDateLessThanEqual(
            LectureStatus status, LocalDate lectureDate);
}
