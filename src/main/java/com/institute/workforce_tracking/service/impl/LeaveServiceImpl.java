package com.institute.workforce_tracking.service.impl;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.request.ApplyLeaveRequest;
import com.institute.workforce_tracking.dto.request.DecisionRequest;
import com.institute.workforce_tracking.dto.response.LeaveBalanceResponse;
import com.institute.workforce_tracking.dto.response.LeaveResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.entity.LeaveRequest;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.enums.LeaveStatus;
import com.institute.workforce_tracking.event.LeaveDecidedEvent;
import com.institute.workforce_tracking.exception.BadRequestException;
import com.institute.workforce_tracking.exception.ResourceNotFoundException;
import com.institute.workforce_tracking.mapper.LeaveMapper;
import com.institute.workforce_tracking.repository.LeaveRequestRepository;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.service.AttendanceService;
import com.institute.workforce_tracking.service.LeaveService;
import com.institute.workforce_tracking.util.DateTimeUtil;
import com.institute.workforce_tracking.util.PageUtils;

/**
 * Default implementation of {@link LeaveService}.
 *
 * <p>Approval integrates with attendance through a DIRECT call (the ON_LEAVE
 * records must be created atomically with the approval), while notification of
 * the decision is announced through an EVENT (delivered after commit; a
 * notification failure must never affect the decision).</p>
 */
@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRepository;
    private final UserRepository userRepository;
    private final LeaveMapper leaveMapper;
    private final AttendanceService attendanceService;
    private final ApplicationEventPublisher eventPublisher;
    private final int annualAllowanceDays;

    public LeaveServiceImpl(LeaveRequestRepository leaveRepository,
                            UserRepository userRepository,
                            LeaveMapper leaveMapper,
                            AttendanceService attendanceService,
                            ApplicationEventPublisher eventPublisher,
                            @Value("${app.leave.annual-allowance-days}") int annualAllowanceDays) {
        this.leaveRepository = leaveRepository;
        this.userRepository = userRepository;
        this.leaveMapper = leaveMapper;
        this.attendanceService = attendanceService;
        this.eventPublisher = eventPublisher;
        this.annualAllowanceDays = annualAllowanceDays;
    }

    @Override
    @Transactional
    public LeaveResponse applyForLeave(String email, ApplyLeaveRequest request) {
        User user = findUserByEmail(email);

        if (request.endDate().isBefore(request.startDate())) {
            throw new BadRequestException("End date cannot be before start date.");
        }
        if (leaveRepository.existsOverlappingActiveRequest(
                user, request.startDate(), request.endDate())) {
            throw new BadRequestException(
                    "You already have a pending or approved leave overlapping these dates.");
        }

        long requestedDays = java.time.temporal.ChronoUnit.DAYS
                .between(request.startDate(), request.endDate()) + 1;
        long remaining = computeBalance(user).remainingDays();
        if (requestedDays > remaining) {
            throw new BadRequestException(
                    "Insufficient leave balance: requested " + requestedDays
                            + " day(s), remaining " + remaining + ".");
        }

        LeaveRequest leave = new LeaveRequest();
        leave.setUser(user);
        leave.setStartDate(request.startDate());
        leave.setEndDate(request.endDate());
        leave.setReason(request.reason());
        leave.setStatus(LeaveStatus.PENDING);

        return leaveMapper.toLeaveResponse(leaveRepository.save(leave));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<LeaveResponse> getMyLeaves(String email, int page, int size) {
        User user = findUserByEmail(email);
        Pageable pageable = PageRequest.of(PageUtils.safePage(page), PageUtils.safeSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<LeaveResponse> result = leaveRepository.findByUser(user, pageable)
                .map(leaveMapper::toLeaveResponse);
        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveBalanceResponse getMyBalance(String email) {
        return computeBalance(findUserByEmail(email));
    }

    @Override
    @Transactional
    public LeaveResponse cancelLeave(String email, Long leaveId) {
        User user = findUserByEmail(email);
        LeaveRequest leave = findLeave(leaveId);

        // Ownership guard: others' requests are treated as nonexistent.
        if (!leave.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("LeaveRequest", "id", leaveId);
        }
        requirePending(leave, "Only pending requests can be cancelled.");

        leave.setStatus(LeaveStatus.CANCELLED);
        return leaveMapper.toLeaveResponse(leaveRepository.save(leave));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<LeaveResponse> getLeavesByStatus(LeaveStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(PageUtils.safePage(page), PageUtils.safeSize(size),
                Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<LeaveResponse> result = leaveRepository.findByStatus(status, pageable)
                .map(leaveMapper::toLeaveResponse);
        return PagedResponse.from(result);
    }

    @Override
    @Transactional
    public LeaveResponse approveLeave(String adminEmail, Long leaveId, DecisionRequest decision) {
        LeaveRequest leave = findLeave(leaveId);
        requirePending(leave, "Only pending requests can be approved.");

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setDecidedBy(findUserByEmail(adminEmail));
        leave.setDecisionComment(decision.comment());

        // Same transaction: approval and its attendance records succeed or
        // fail together. This is why it is a direct call, not an event.
        attendanceService.markLeaveDays(leave.getUser(), leave.getStartDate(), leave.getEndDate());

        // Announce the decision. Delivered AFTER COMMIT to the notification
        // listener — a notification failure can never affect the approval.
        eventPublisher.publishEvent(new LeaveDecidedEvent(
                leave.getUser().getId(), leave.getUser().getEmail(), true,
                leave.getStartDate(), leave.getEndDate()));

        return leaveMapper.toLeaveResponse(leaveRepository.save(leave));
    }

    @Override
    @Transactional
    public LeaveResponse rejectLeave(String adminEmail, Long leaveId, DecisionRequest decision) {
        LeaveRequest leave = findLeave(leaveId);
        requirePending(leave, "Only pending requests can be rejected.");

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setDecidedBy(findUserByEmail(adminEmail));
        leave.setDecisionComment(decision.comment());

        eventPublisher.publishEvent(new LeaveDecidedEvent(
                leave.getUser().getId(), leave.getUser().getEmail(), false,
                leave.getStartDate(), leave.getEndDate()));

        return leaveMapper.toLeaveResponse(leaveRepository.save(leave));
    }

    private LeaveBalanceResponse computeBalance(User user) {
        int year = DateTimeUtil.today().getYear();
        long used = leaveRepository
                .findApprovedInYear(user, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31))
                .stream()
                .mapToLong(LeaveRequest::getTotalDays)
                .sum();
        return new LeaveBalanceResponse(year, annualAllowanceDays, used,
                Math.max(annualAllowanceDays - used, 0));
    }

    private LeaveRequest findLeave(Long id) {
        return leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void requirePending(LeaveRequest leave, String message) {
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException(message);
        }
    }
}
