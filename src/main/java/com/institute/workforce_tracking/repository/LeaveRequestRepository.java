package com.institute.workforce_tracking.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.institute.workforce_tracking.entity.LeaveRequest;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.enums.LeaveStatus;

/**
 * Data-access layer for {@link LeaveRequest} records.
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    /**
     * A page of one user's leave requests (their history).
     */
    Page<LeaveRequest> findByUser(User user, Pageable pageable);

    /**
     * Admin queue: a page of all requests with a given status, with each
     * request's user pre-fetched for display.
     */
    @EntityGraph(attributePaths = "user")
    Page<LeaveRequest> findByStatus(LeaveStatus status, Pageable pageable);

    /**
     * Does the user already have a PENDING or APPROVED request that overlaps
     * the given date range?
     *
     * <p>Two inclusive ranges [s1,e1] and [s2,e2] overlap exactly when
     * {@code s1 <= e2 AND e1 >= s2}. This cross-row, cross-column comparison
     * cannot be expressed by derived method names, so it is written in JPQL.</p>
     *
     * @param user      the requesting user
     * @param startDate proposed first day of leave
     * @param endDate   proposed last day of leave
     * @return true if an active (pending or approved) overlapping request exists
     */
    @Query("""
            SELECT COUNT(lr) > 0
            FROM LeaveRequest lr
            WHERE lr.user = :user
              AND lr.status IN (com.institute.workforce_tracking.enums.LeaveStatus.PENDING,
                                com.institute.workforce_tracking.enums.LeaveStatus.APPROVED)
              AND lr.startDate <= :endDate
              AND lr.endDate >= :startDate
            """)
    boolean existsOverlappingActiveRequest(@Param("user") User user,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * Total number of approved leave days a user has taken within a year,
     * used to compute the remaining balance.
     *
     * <p>Day counts are computed in Java from the returned requests rather
     * than in SQL, keeping the inclusive-day arithmetic in one place
     * ({@code LeaveRequest#getTotalDays}).</p>
     *
     * @param user      the user
     * @param yearStart first day of the year
     * @param yearEnd   last day of the year
     * @return the user's approved requests that fall within the year
     */
    @Query("""
            SELECT lr
            FROM LeaveRequest lr
            WHERE lr.user = :user
              AND lr.status = com.institute.workforce_tracking.enums.LeaveStatus.APPROVED
              AND lr.startDate >= :yearStart
              AND lr.startDate <= :yearEnd
            """)
    java.util.List<LeaveRequest> findApprovedInYear(@Param("user") User user,
                                                    @Param("yearStart") LocalDate yearStart,
                                                    @Param("yearEnd") LocalDate yearEnd);
}
