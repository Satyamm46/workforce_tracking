package com.institute.workforce_tracking.service;

import java.time.LocalDate;
import java.util.List;

import com.institute.workforce_tracking.dto.request.WorkPlanRequest;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.dto.response.WorkPlanResponse;

/**
 * Business operations for next-day work plans.
 *
 * <p>Admins and Employees declare a day before: when they will start and
 * finish, and what they will work on. A plan for tomorrow can be created or
 * edited until midnight; a plan submitted on the day itself (to unblock a
 * forgotten submission) is recorded as late. Checking in requires a plan for
 * today — enforced by the attendance module.</p>
 */
public interface WorkPlanService {

    /**
     * Creates or updates the caller's plan for tomorrow.
     *
     * @param email   the caller
     * @param request the planned times and work description
     * @return the saved plan
     */
    WorkPlanResponse submitTomorrowPlan(String email, WorkPlanRequest request);

    /**
     * Creates the caller's plan for TODAY, marked late. Allowed only when no
     * plan exists yet — the escape hatch for a forgotten evening submission,
     * since check-in is blocked without a plan.
     */
    WorkPlanResponse submitTodayPlanLate(String email, WorkPlanRequest request);

    /** The caller's plan for a specific day, or 404. */
    WorkPlanResponse getMyPlan(String email, LocalDate date);

    /** A page of the caller's plans, newest day first. */
    PagedResponse<WorkPlanResponse> getMyPlans(String email, int page, int size);

    /** Admin: every plan submitted for one day. */
    PagedResponse<WorkPlanResponse> getPlansByDate(LocalDate date, int page, int size);

    /**
     * Admin: names of active Admins/Employees who have NOT submitted a plan
     * for the given day.
     */
    List<String> getMissingSubmitters(LocalDate date);

    /** Whether the user must have a plan to check in (Admin/Employee only). */
    boolean isPlanRequired(com.institute.workforce_tracking.entity.User user);

    /** Whether the user has a plan for the given day. */
    boolean hasPlan(com.institute.workforce_tracking.entity.User user, LocalDate date);

    /**
     * The start time the user declared for the given day, if a plan exists.
     * Backs the late-arrival check at check-in.
     */
    java.util.Optional<java.time.LocalTime> getPlannedStartTime(
            com.institute.workforce_tracking.entity.User user, LocalDate date);
}
