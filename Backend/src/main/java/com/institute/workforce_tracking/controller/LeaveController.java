package com.institute.workforce_tracking.controller;

import com.institute.workforce_tracking.constants.ApiConstants;
import com.institute.workforce_tracking.dto.request.ApplyLeaveRequest;
import com.institute.workforce_tracking.dto.request.DecisionRequest;
import com.institute.workforce_tracking.dto.response.LeaveBalanceResponse;
import com.institute.workforce_tracking.dto.response.LeaveResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.enums.LeaveStatus;
import com.institute.workforce_tracking.dto.response.ApiResponse;
import com.institute.workforce_tracking.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for the leave workflow. Employee operations derive identity
 * from the token; review operations are role-restricted.
 */
@RestController
@RequestMapping(ApiConstants.LEAVES_BASE)
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    /** Submits a new leave request for the caller. */
    @PostMapping
    public ResponseEntity<ApiResponse<LeaveResponse>> applyForLeave(
            Authentication authentication,
            @Valid @RequestBody ApplyLeaveRequest request) {

        LeaveResponse leave = leaveService.applyForLeave(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Leave request submitted", leave));
    }

    /** Returns a page of the caller's leave requests, newest first. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PagedResponse<LeaveResponse>>> getMyLeaves(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<LeaveResponse> leaves =
                leaveService.getMyLeaves(authentication.getName(), page, size);
        return ResponseEntity.ok(ApiResponse.of("Leave history retrieved", leaves));
    }

    /** Returns the caller's leave balance for the current year. */
    @GetMapping("/me/balance")
    public ResponseEntity<ApiResponse<LeaveBalanceResponse>> getMyBalance(
            Authentication authentication) {

        LeaveBalanceResponse balance = leaveService.getMyBalance(authentication.getName());
        return ResponseEntity.ok(ApiResponse.of("Leave balance retrieved", balance));
    }

    /** Cancels one of the caller's own pending requests. */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<LeaveResponse>> cancelLeave(
            Authentication authentication,
            @PathVariable Long id) {

        LeaveResponse leave = leaveService.cancelLeave(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.of("Leave request cancelled", leave));
    }

    /** Admin review queue, filtered by status (defaults to PENDING). */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<LeaveResponse>>> getLeavesByStatus(
            @RequestParam(defaultValue = "PENDING") LeaveStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PagedResponse<LeaveResponse> leaves = leaveService.getLeavesByStatus(status, page, size);
        return ResponseEntity.ok(ApiResponse.of("Leave requests retrieved", leaves));
    }

    /** Approves a pending request (generates ON_LEAVE attendance records). */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<LeaveResponse>> approveLeave(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody DecisionRequest decision) {

        LeaveResponse leave =
                leaveService.approveLeave(authentication.getName(), id, decision);
        return ResponseEntity.ok(ApiResponse.of("Leave request approved", leave));
    }

    /** Rejects a pending request. */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<LeaveResponse>> rejectLeave(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody DecisionRequest decision) {

        LeaveResponse leave =
                leaveService.rejectLeave(authentication.getName(), id, decision);
        return ResponseEntity.ok(ApiResponse.of("Leave request rejected", leave));
    }
}
