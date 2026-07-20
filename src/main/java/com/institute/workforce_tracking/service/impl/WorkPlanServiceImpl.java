package com.institute.workforce_tracking.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.request.WorkPlanRequest;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.dto.response.WorkPlanResponse;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.entity.WorkPlan;
import com.institute.workforce_tracking.enums.DeadlineType;
import com.institute.workforce_tracking.enums.Role;
import com.institute.workforce_tracking.exception.BadRequestException;
import com.institute.workforce_tracking.exception.DuplicateResourceException;
import com.institute.workforce_tracking.exception.ResourceNotFoundException;
import com.institute.workforce_tracking.mapper.WorkPlanMapper;
import com.institute.workforce_tracking.repository.DeadlineExtensionRepository;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.repository.WorkPlanRepository;
import com.institute.workforce_tracking.service.WorkPlanService;
import com.institute.workforce_tracking.util.DateTimeUtil;
import com.institute.workforce_tracking.util.PageUtils;

/**
 * Default implementation of {@link WorkPlanService}.
 *
 * <p>The server owns the calendar: "tomorrow" and "today" are computed here
 * from the institute clock, never accepted from the client, so nobody can
 * back-date or forward-date a plan.</p>
 */
@Service
public class WorkPlanServiceImpl implements WorkPlanService {

    private final WorkPlanRepository workPlanRepository;
    private final UserRepository userRepository;
    private final WorkPlanMapper workPlanMapper;
    private final DeadlineExtensionRepository deadlineExtensionRepository;

    public WorkPlanServiceImpl(WorkPlanRepository workPlanRepository,
                               UserRepository userRepository,
                               WorkPlanMapper workPlanMapper,
                               DeadlineExtensionRepository deadlineExtensionRepository) {
        this.workPlanRepository = workPlanRepository;
        this.userRepository = userRepository;
        this.workPlanMapper = workPlanMapper;
        this.deadlineExtensionRepository = deadlineExtensionRepository;
    }

    @Override
    @Transactional
    public WorkPlanResponse submitTomorrowPlan(String email, WorkPlanRequest request) {
        User user = findUserByEmail(email);
        validateTimes(request);

        LocalDate tomorrow = DateTimeUtil.today().plusDays(1);

        // Editable until midnight: an existing plan for tomorrow is updated
        // in place rather than rejected.
        WorkPlan plan = workPlanRepository.findByUserAndPlanDate(user, tomorrow)
                .orElseGet(WorkPlan::new);
        plan.setUser(user);
        plan.setPlanDate(tomorrow);
        applyRequest(plan, request);
        plan.setSubmittedLate(false);

        return workPlanMapper.toWorkPlanResponse(workPlanRepository.save(plan));
    }

    @Override
    @Transactional
    public WorkPlanResponse submitTodayPlanLate(String email, WorkPlanRequest request) {
        User user = findUserByEmail(email);
        validateTimes(request);

        LocalDate today = DateTimeUtil.today();
        if (workPlanRepository.existsByUserAndPlanDate(user, today)) {
            throw new DuplicateResourceException(
                    "A plan for today already exists — late submission is only for missing plans.");
        }

        WorkPlan plan = new WorkPlan();
        plan.setUser(user);
        plan.setPlanDate(today);
        applyRequest(plan, request);

        // An admin-granted extension excuses the lateness: the plan is
        // accepted as if submitted on time.
        boolean extended = deadlineExtensionRepository
                .findByUserAndTypeAndTargetDate(user, DeadlineType.WORK_PLAN, today)
                .isPresent();
        plan.setSubmittedLate(!extended);

        return workPlanMapper.toWorkPlanResponse(workPlanRepository.save(plan));
    }

    @Override
    @Transactional(readOnly = true)
    public WorkPlanResponse getMyPlan(String email, LocalDate date) {
        User user = findUserByEmail(email);
        return workPlanRepository.findByUserAndPlanDate(user, date)
                .map(workPlanMapper::toWorkPlanResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Work plan", "date", date));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WorkPlanResponse> getMyPlans(String email, int page, int size) {
        User user = findUserByEmail(email);
        Pageable pageable = PageRequest.of(PageUtils.safePage(page), PageUtils.safeSize(size),
                Sort.by(Sort.Direction.DESC, "planDate"));
        Page<WorkPlanResponse> result = workPlanRepository.findByUser(user, pageable)
                .map(workPlanMapper::toWorkPlanResponse);
        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WorkPlanResponse> getPlansByDate(LocalDate date, int page, int size) {
        Pageable pageable = PageRequest.of(PageUtils.safePage(page), PageUtils.safeSize(size),
                Sort.by(Sort.Direction.ASC, "plannedStartTime"));
        Page<WorkPlanResponse> result = workPlanRepository.findByPlanDate(date, pageable)
                .map(workPlanMapper::toWorkPlanResponse);
        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getMissingSubmitters(LocalDate date) {
        List<Long> submittedUserIds = workPlanRepository.findByPlanDate(date).stream()
                .map(plan -> plan.getUser().getId())
                .toList();

        return userRepository.findAll().stream()
                .filter(User::isEnabled)
                .filter(this::isPlanRequired)
                .filter(user -> !submittedUserIds.contains(user.getId()))
                .map(User::getFullName)
                .sorted()
                .toList();
    }

    @Override
    public boolean isPlanRequired(User user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.EMPLOYEE;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPlan(User user, LocalDate date) {
        return workPlanRepository.existsByUserAndPlanDate(user, date);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<java.time.LocalTime> getPlannedStartTime(User user, LocalDate date) {
        return workPlanRepository.findByUserAndPlanDate(user, date)
                .map(WorkPlan::getPlannedStartTime);
    }

    /** Copies the request fields onto the entity. */
    private void applyRequest(WorkPlan plan, WorkPlanRequest request) {
        plan.setPlannedStartTime(request.plannedStartTime());
        plan.setPlannedEndTime(request.plannedEndTime());
        plan.setWorkDescription(request.workDescription().trim());
    }

    /** End must come after start — same-day plans only. */
    private void validateTimes(WorkPlanRequest request) {
        if (!request.plannedEndTime().isAfter(request.plannedStartTime())) {
            throw new BadRequestException("Planned end time must be after the start time.");
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
