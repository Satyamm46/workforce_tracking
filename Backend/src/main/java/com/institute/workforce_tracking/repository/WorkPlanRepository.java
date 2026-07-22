package com.institute.workforce_tracking.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.entity.WorkPlan;

/**
 * Data-access layer for {@link WorkPlan} records.
 */
@Repository
public interface WorkPlanRepository extends JpaRepository<WorkPlan, Long> {

    /** A user's plan for one day, if submitted. At most one (unique constraint). */
    Optional<WorkPlan> findByUserAndPlanDate(User user, LocalDate planDate);

    /** Whether the user has a plan for the day — backs the check-in guard. */
    boolean existsByUserAndPlanDate(User user, LocalDate planDate);

    /** A user's plan history, sorted via the pageable. */
    Page<WorkPlan> findByUser(User user, Pageable pageable);

    /** All plans for one day with users pre-fetched (admin view). */
    @EntityGraph(attributePaths = "user")
    Page<WorkPlan> findByPlanDate(LocalDate planDate, Pageable pageable);

    /** All plans for one day, unpaged, for the missing-submitters diff. */
    @EntityGraph(attributePaths = "user")
    java.util.List<WorkPlan> findByPlanDate(LocalDate planDate);
}
