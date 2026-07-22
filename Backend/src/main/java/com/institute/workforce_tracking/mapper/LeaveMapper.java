package com.institute.workforce_tracking.mapper;

import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.dto.response.LeaveResponse;
import com.institute.workforce_tracking.entity.LeaveRequest;

/**
 * Converts {@link LeaveRequest} entities to their outbound representation.
 *
 * <p>Must be invoked inside a transaction: it touches the lazy {@code user}
 * and (when decided) {@code decidedBy} associations.</p>
 */
@Component
public class LeaveMapper {

    public LeaveResponse toLeaveResponse(LeaveRequest leave) {
        return new LeaveResponse(
                leave.getId(),
                leave.getUser().getId(),
                leave.getUser().getFullName(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getTotalDays(),
                leave.getReason(),
                leave.getStatus(),
                leave.getCreatedAt(),
                leave.getDecidedBy() != null ? leave.getDecidedBy().getFullName() : null,
                leave.getDecisionComment()
        );
    }
}
