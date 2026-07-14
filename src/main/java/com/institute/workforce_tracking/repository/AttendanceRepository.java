package com.institute.workforce_tracking.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.institute.workforce_tracking.entity.Attendance;
import com.institute.workforce_tracking.entity.User;

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
}