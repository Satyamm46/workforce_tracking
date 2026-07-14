package com.institute.workforce_tracking.service;

import com.institute.workforce_tracking.dto.request.ApplyLeaveRequest;
import com.institute.workforce_tracking.dto.request.DecisionRequest;
import com.institute.workforce_tracking.dto.response.LeaveBalanceResponse;
import com.institute.workforce_tracking.dto.response.LeaveResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.enums.LeaveStatus;

/**
 * Business operations for the leave workflow: employees apply, view balance
 * and history, and may cancel pending requests; admins review and decide.
 */
public interface LeaveService {

    LeaveResponse applyForLeave(String email, ApplyLeaveRequest request);

    PagedResponse<LeaveResponse> getMyLeaves(String email, int page, int size);

    LeaveBalanceResponse getMyBalance(String email);

    LeaveResponse cancelLeave(String email, Long leaveId);

    PagedResponse<LeaveResponse> getLeavesByStatus(LeaveStatus status, int page, int size);

    LeaveResponse approveLeave(String adminEmail, Long leaveId, DecisionRequest decision);

    LeaveResponse rejectLeave(String adminEmail, Long leaveId, DecisionRequest decision);
}
