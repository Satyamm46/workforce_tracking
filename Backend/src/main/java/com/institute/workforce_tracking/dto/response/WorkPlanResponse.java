package com.institute.workforce_tracking.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Outbound view of one work plan.
 *
 * @param id               the plan's id
 * @param userId           the owner's id
 * @param userFullName     the owner's display name (admin views)
 * @param planDate         the day being planned
 * @param plannedStartTime intended start-of-work time
 * @param plannedEndTime   intended end-of-work time
 * @param workDescription  what the user will work on
 * @param submittedLate    true when submitted on the planned day itself
 */
public record WorkPlanResponse(
        Long id,
        Long userId,
        String userFullName,
        LocalDate planDate,
        LocalTime plannedStartTime,
        LocalTime plannedEndTime,
        String workDescription,
        boolean submittedLate
) {
}
