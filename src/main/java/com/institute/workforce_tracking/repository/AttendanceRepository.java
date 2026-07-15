package com.institute.workforce_tracking.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.institute.workforce_tracking.dto.response.AttendanceReportRow;
import com.institute.workforce_tracking.entity.Attendance;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.enums.AttendanceStatus;

/**
 * Data-access layer for {@link Attendance} records.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /**
     * Finds a user's attendance record for a specific day.
     *
     * <p>Backs the idempotent clock-in (does today's record already exist?),
     * the clock-out (load today's record to complete it), and the "my status
     * today" view. At most one row can match, guaranteed by the
     * (user, work_date) unique constraint.</p>
     *
     * @param user     the employee
     * @param workDate the day
     * @return the record for that user and day, if any
     */
    Optional<Attendance> findByUserAndWorkDate(User user, LocalDate workDate);

    /**
     * Returns a page of one user's attendance history.
     *
     * <p>Sorting (e.g. newest day first) is supplied through the
     * {@link Pageable} by the service.</p>
     *
     * @param user     the employee
     * @param pageable page number, size, and sort
     * @return a page of the user's records
     */
    Page<Attendance> findByUser(User user, Pageable pageable);

    /**
     * Returns a page of ALL users' attendance for one day, with each record's
     * user loaded in the same query.
     *
     * <p>The {@code @EntityGraph} instructs JPA to fetch the lazy {@code user}
     * association eagerly for THIS query only (a join), preventing the N+1
     * problem when the admin view renders user names for a page of records.</p>
     *
     * @param workDate the day to report on
     * @param pageable page number, size, and sort
     * @return a page of records with their users pre-fetched
     */
    @EntityGraph(attributePaths = "user")
    Page<Attendance> findByWorkDate(LocalDate workDate, Pageable pageable);

    /** How many attendance records exist for a day in a given status. */
    long countByWorkDateAndStatus(LocalDate workDate, AttendanceStatus status);
    
        /**
     * Builds the per-employee attendance summary for a date range in a single
     * aggregate query: one result row per employee who has any attendance
     * record in the period.
     *
     * <p>Uses a JPQL constructor expression ("SELECT new ...") so the database
     * aggregates and the result arrives already in response shape — no
     * entities are loaded. Present vs leave days are separated with
     * conditional aggregation (SUM over a CASE).</p>
     */
    @Query("""
            SELECT new com.institute.workforce_tracking.dto.response.AttendanceReportRow(
                u.id,
                u.fullName,
                SUM(CASE WHEN a.status <> com.institute.workforce_tracking.enums.AttendanceStatus.ON_LEAVE
                         THEN 1L ELSE 0L END),
                SUM(CASE WHEN a.status = com.institute.workforce_tracking.enums.AttendanceStatus.ON_LEAVE
                         THEN 1L ELSE 0L END),
                SUM(COALESCE(a.workingMinutes, 0)),
                SUM(a.totalBreakMinutes)
            )
            FROM Attendance a
            JOIN a.user u
            WHERE a.workDate BETWEEN :startDate AND :endDate
            GROUP BY u.id, u.fullName
            ORDER BY u.fullName
            """)
    java.util.List<AttendanceReportRow> buildAttendanceReport(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

}