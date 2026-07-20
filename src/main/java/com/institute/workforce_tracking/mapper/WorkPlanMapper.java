package com.institute.workforce_tracking.mapper;

import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.dto.response.WorkPlanResponse;
import com.institute.workforce_tracking.entity.WorkPlan;

/**
 * Converts {@link WorkPlan} entities to their outbound DTO.
 */
@Component
public class WorkPlanMapper {

    public WorkPlanResponse toWorkPlanResponse(WorkPlan plan) {
        return new WorkPlanResponse(
                plan.getId(),
                plan.getUser().getId(),
                plan.getUser().getFullName(),
                plan.getPlanDate(),
                plan.getPlannedStartTime(),
                plan.getPlannedEndTime(),
                plan.getWorkDescription(),
                plan.isSubmittedLate()
        );
    }
}
