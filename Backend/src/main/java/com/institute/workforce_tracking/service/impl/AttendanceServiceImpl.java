package com.institute.workforce_tracking.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.response.AttendanceResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.entity.Attendance;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.entity.WorkBreak;
import com.institute.workforce_tracking.entity.WorkPlan;
import com.institute.workforce_tracking.enums.AttendanceStatus;
import com.institute.workforce_tracking.event.AttendanceAutoActionEvent;
import com.institute.workforce_tracking.event.LateArrivalEvent;
import com.institute.workforce_tracking.event.OvertimeCheckedOutEvent;
import com.institute.workforce_tracking.event.OvertimeReminderEvent;
import com.institute.workforce_tracking.event.WorkStartReminderEvent;
import com.institute.workforce_tracking.exception.BadRequestException;
import com.institute.workforce_tracking.exception.ResourceNotFoundException;
import com.institute.workforce_tracking.mapper.AttendanceMapper;
import com.institute.workforce_tracking.repository.AttendanceRepository;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.repository.WorkBreakRepository;
import com.institute.workforce_tracking.repository.WorkPlanRepository;
import com.institute.workforce_tracking.service.AttendanceService;
import com.institute.workforce_tracking.service.PresenceService;
import com.institute.workforce_tracking.service.WorkPlanService;
import com.institute.workforce_tracking.util.DateTimeUtil;
import com.institute.workforce_tracking.util.PageUtils;

/**
 * Default implementation of {@link AttendanceService}.
 *
 * <p>Owns the working-day state machine: WORKING ⇄ ON_BREAK → CHECKED_OUT.
 * Every transition is guarded; invalid transitions surface as 400s.</p>
 */
@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceServiceImpl.class);

    /** Minutes past the planned start before a check-in counts as late. */
    private static final int LATE_GRACE_MINUTES = 15;

    /** Fire the work-start reminder this many minutes before the planned start. */
    private static final int START_REMINDER_LEAD_MINUTES = 5;

    /** Warn the employee this many minutes before their overtime window closes. */
    private static final int OVERTIME_WARN_LEAD_MINUTES = 5;

    private final AttendanceRepository attendanceRepository;
    private final WorkBreakRepository workBreakRepository;
    private final UserRepository userRepository;
    private final WorkPlanRepository workPlanRepository;
    private final AttendanceMapper attendanceMapper;
    private final PresenceService presenceService;
    private final WorkPlanService workPlanService;
    private final ApplicationEventPublisher eventPublisher;

    /** Length of each overtime window, in minutes (configurable). */
    private final long overtimeWindowMinutes;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 WorkBreakRepository workBreakRepository,
                                 UserRepository userRepository,
                                 WorkPlanRepository workPlanRepository,
                                 AttendanceMapper attendanceMapper,
                                 PresenceService presenceService,
                                 WorkPlanService workPlanService,
                                 ApplicationEventPublisher eventPublisher,
                                 @Value("${app.attendance.overtime-window-minutes:30}")
                                 long overtimeWindowMinutes) {
        this.attendanceRepository = attendanceRepository;
        this.workBreakRepository = workBreakRepository;
        this.userRepository = userRepository;
        this.workPlanRepository = workPlanRepository;
        this.attendanceMapper = attendanceMapper;
        this.presenceService = presenceService;
        this.workPlanService = workPlanService;
        this.eventPublisher = eventPublisher;
        this.overtimeWindowMinutes = overtimeWindowMinutes;
    }

    @Override
    @Transactional
    public AttendanceResponse checkIn(String email) {
        User user = findUserByEmail(email);
        LocalDate today = DateTimeUtil.today();
        LocalDateTime now = DateTimeUtil.now();

        // Admins and Employees must have declared today's plan (submitted the
        // evening before, or late via the plan page) before starting the day.
        if (workPlanService.isPlanRequired(user) && !workPlanService.hasPlan(user, today)) {
            throw new BadRequestException(
                    "You have not submitted a work plan for today. "
                            + "Submit it on the My Schedule page, then check in.");
        }

        Attendance attendance = attendanceRepository.findByUserAndWorkDate(user, today)
                .orElse(null);

        if (attendance == null) {
            // First check-in of the day. Deliberately manual — logging in does
            // NOT start the working day; pressing Check In does.
            attendance = new Attendance();
            attendance.setUser(user);
            attendance.setWorkDate(today);
            attendance.setLoginTime(now);
            attendance.setStatus(AttendanceStatus.WORKING);
            applyLateArrivalPenalty(user, attendance, today, now);
            log.info("Check-in recorded for {} on {}", email, today);
            return attendanceMapper.toAttendanceResponse(attendanceRepository.save(attendance));
        }

        if (attendance.getStatus() != AttendanceStatus.CHECKED_OUT) {
            throw new BadRequestException("You are already checked in for today.");
        }

        // Reopen the day: the time between clock-out and now is recorded as a
        // completed break so the final working-minutes math stays correct
        // (working = last logout − first login − total breaks).
        WorkBreak awayGap = new WorkBreak();
        awayGap.setAttendance(attendance);
        awayGap.setStartTime(attendance.getLogoutTime());
        awayGap.setEndTime(now);
        long awayMinutes = Duration.between(attendance.getLogoutTime(), now).toMinutes();
        awayGap.setDurationMinutes((int) Math.max(awayMinutes, 0));
        workBreakRepository.save(awayGap);

        attendance.setTotalBreakMinutes(
                attendance.getTotalBreakMinutes() + awayGap.getDurationMinutes());
        attendance.setLogoutTime(null);
        attendance.setWorkingMinutes(null);
        attendance.setStatus(AttendanceStatus.WORKING);
        // Reopening starts a clean slate; any old overtime window no longer applies.
        attendance.setOvertimeDeadline(null);
        attendance.setOvertimeReminderSent(false);

        log.info("Re-check-in for {} after {} minutes away", email, awayGap.getDurationMinutes());
        return attendanceMapper.toAttendanceResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional
    public AttendanceResponse clockOut(String email) {
        Attendance attendance = getTodayAttendance(email);

        if (attendance.getStatus() == AttendanceStatus.CHECKED_OUT) {
            throw new BadRequestException("You have already clocked out for today.");
        }

        LocalDateTime logoutTime = DateTimeUtil.now();

        // Leaving while on a break ends the break at the moment of departure —
        // MUST happen before the working-minutes computation below.
        closeOpenBreak(attendance, logoutTime);

        long workedMinutes = Duration.between(attendance.getLoginTime(), logoutTime).toMinutes()
                - attendance.getTotalBreakMinutes();

        attendance.setLogoutTime(logoutTime);
        attendance.setWorkingMinutes((int) Math.max(workedMinutes, 0));
        attendance.setStatus(AttendanceStatus.CHECKED_OUT);

        return attendanceMapper.toAttendanceResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional
    public AttendanceResponse startBreak(String email) {
        Attendance attendance = getTodayAttendance(email);

        if (attendance.getStatus() == AttendanceStatus.CHECKED_OUT) {
            throw new BadRequestException("Cannot start a break after clocking out.");
        }
        if (attendance.getStatus() == AttendanceStatus.ON_BREAK) {
            throw new BadRequestException("You are already on a break.");
        }

        WorkBreak workBreak = new WorkBreak();
        workBreak.setAttendance(attendance);
        workBreak.setStartTime(DateTimeUtil.now());
        workBreakRepository.save(workBreak);

        attendance.setStatus(AttendanceStatus.ON_BREAK);
        return attendanceMapper.toAttendanceResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional
    public AttendanceResponse resumeWork(String email) {
        Attendance attendance = getTodayAttendance(email);

        if (attendance.getStatus() != AttendanceStatus.ON_BREAK) {
            throw new BadRequestException("You are not on a break.");
        }

        closeOpenBreak(attendance, DateTimeUtil.now());

        attendance.setStatus(AttendanceStatus.WORKING);
        return attendanceMapper.toAttendanceResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getMyTodayAttendance(String email) {
        return attendanceMapper.toAttendanceResponse(getTodayAttendance(email));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AttendanceResponse> getMyAttendanceHistory(String email, int page, int size) {
        User user = findUserByEmail(email);

        Pageable pageable = PageRequest.of(PageUtils.safePage(page), PageUtils.safeSize(size),
                Sort.by(Sort.Direction.DESC, "workDate"));

        Page<AttendanceResponse> result = attendanceRepository.findByUser(user, pageable)
                .map(attendanceMapper::toAttendanceResponse);

        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AttendanceResponse> getAttendanceByDate(LocalDate date, int page, int size) {
        Pageable pageable = PageRequest.of(PageUtils.safePage(page), PageUtils.safeSize(size),
                Sort.by(Sort.Direction.ASC, "loginTime"));

        Page<AttendanceResponse> result = attendanceRepository.findByWorkDate(date, pageable)
                .map(attendanceMapper::toAttendanceResponse);

        return PagedResponse.from(result);
    }

        @Override
    @Transactional
    public void markLeaveDays(User user, LocalDate startDate, LocalDate endDate) {
        for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            if (attendanceRepository.findByUserAndWorkDate(user, day).isEmpty()) {
                Attendance attendance = new Attendance();
                attendance.setUser(user);
                attendance.setWorkDate(day);
                attendance.setStatus(AttendanceStatus.ON_LEAVE);
                attendance.setWorkingMinutes(0);
                attendanceRepository.save(attendance);
            }
            // A day that already has a record (the employee worked) is left untouched.
        }
    }


    @Override
    @Transactional
    public int autoBreakAbsentUsers(long graceSeconds) {
        List<Attendance> working = attendanceRepository
                .findByWorkDateAndStatus(DateTimeUtil.today(), AttendanceStatus.WORKING);

        int placed = 0;
        for (Attendance attendance : working) {
            String email = attendance.getUser().getEmail();
            if (!presenceService.isOffline(email, graceSeconds)) {
                continue;
            }

            WorkBreak workBreak = new WorkBreak();
            workBreak.setAttendance(attendance);
            workBreak.setStartTime(DateTimeUtil.now());
            workBreak.setAutoStarted(true);
            workBreakRepository.save(workBreak);

            attendance.setStatus(AttendanceStatus.ON_BREAK);
            attendanceRepository.save(attendance);

            eventPublisher.publishEvent(new AttendanceAutoActionEvent(
                    attendance.getUser().getId(), email, false));
            placed++;
            log.info("Auto-break started for {} (offline beyond grace period)", email);
        }
        return placed;
    }

    @Override
    @Transactional
    public int autoCheckoutOverdueBreaks(long maxBreakMinutes) {
        LocalDateTime cutoff = DateTimeUtil.now().minusMinutes(maxBreakMinutes);
        List<WorkBreak> overdue =
                workBreakRepository.findByEndTimeIsNullAndAutoStartedTrueAndStartTimeBefore(cutoff);

        int checkedOut = 0;
        for (WorkBreak workBreak : overdue) {
            Attendance attendance = workBreak.getAttendance();
            LocalDateTime logoutTime = DateTimeUtil.now();

            closeOpenBreak(attendance, logoutTime);

            long workedMinutes = Duration.between(attendance.getLoginTime(), logoutTime).toMinutes()
                    - attendance.getTotalBreakMinutes();
            attendance.setLogoutTime(logoutTime);
            attendance.setWorkingMinutes((int) Math.max(workedMinutes, 0));
            attendance.setStatus(AttendanceStatus.CHECKED_OUT);
            attendanceRepository.save(attendance);

            eventPublisher.publishEvent(new AttendanceAutoActionEvent(
                    attendance.getUser().getId(), attendance.getUser().getEmail(), true));
            checkedOut++;
            log.info("Auto-checkout for {} (auto-break exceeded {} minutes)",
                    attendance.getUser().getEmail(), maxBreakMinutes);
        }
        return checkedOut;
    }

    @Override
    @Transactional
    public int publishStartReminders() {
        LocalDate today = DateTimeUtil.today();
        LocalTime now = DateTimeUtil.now().toLocalTime();

        // Plans for today whose start is within the lead window, not yet
        // reminded, and whose owner has not already checked in.
        List<WorkPlan> due = workPlanRepository.findByPlanDate(today).stream()
                .filter(plan -> !plan.isStartReminderSent())
                .filter(plan -> plan.getPlannedStartTime().isAfter(now))
                .filter(plan -> !plan.getPlannedStartTime()
                        .isAfter(now.plusMinutes(START_REMINDER_LEAD_MINUTES)))
                .filter(plan -> attendanceRepository
                        .findByUserAndWorkDate(plan.getUser(), today)
                        .map(a -> a.getLoginTime() == null)
                        .orElse(true))
                .toList();

        for (WorkPlan plan : due) {
            User user = plan.getUser();
            eventPublisher.publishEvent(new WorkStartReminderEvent(
                    user.getId(), user.getEmail(), user.getFullName(),
                    plan.getPlannedStartTime()));
            plan.setStartReminderSent(true);
        }
        workPlanRepository.saveAll(due);
        return due.size();
    }

    @Override
    @Transactional
    public int processOvertimeWindows() {
        LocalDate today = DateTimeUtil.today();
        LocalDateTime now = DateTimeUtil.now();
        List<Attendance> working = attendanceRepository
                .findByWorkDateAndStatus(today, AttendanceStatus.WORKING);

        int actions = 0;
        for (Attendance attendance : working) {
            User user = attendance.getUser();

            // Overtime is measured against the declared logout time; skip
            // anyone without a plan (e.g. Super Admins, teachers).
            LocalTime plannedEnd = workPlanRepository
                    .findByUserAndPlanDate(user, today)
                    .map(WorkPlan::getPlannedEndTime)
                    .orElse(null);
            if (plannedEnd == null) {
                continue;
            }

            LocalDateTime plannedEndToday = today.atTime(plannedEnd);
            if (now.isBefore(plannedEndToday)) {
                continue; // not into overtime yet
            }

            // First time past the planned end: open a fresh window from now.
            if (attendance.getOvertimeDeadline() == null) {
                attendance.setOvertimeDeadline(now.plusMinutes(overtimeWindowMinutes));
                attendance.setOvertimeReminderSent(false);
                attendanceRepository.save(attendance);
            }

            LocalDateTime deadline = attendance.getOvertimeDeadline();

            if (!now.isBefore(deadline)) {
                // Window closed with no extension → auto-checkout.
                closeOpenBreak(attendance, now);
                long workedMinutes = Duration.between(attendance.getLoginTime(), now).toMinutes()
                        - attendance.getTotalBreakMinutes();
                attendance.setLogoutTime(now);
                attendance.setWorkingMinutes((int) Math.max(workedMinutes, 0));
                attendance.setStatus(AttendanceStatus.CHECKED_OUT);
                attendance.setOvertimeDeadline(null);
                attendanceRepository.save(attendance);

                eventPublisher.publishEvent(new OvertimeCheckedOutEvent(user.getId(), user.getEmail()));
                actions++;
                log.info("Overtime auto-checkout for {} (window closed unextended)", user.getEmail());
            } else if (!attendance.isOvertimeReminderSent()
                    && !now.isBefore(deadline.minusMinutes(OVERTIME_WARN_LEAD_MINUTES))) {
                // Within the warning lead → remind once for this window.
                eventPublisher.publishEvent(new OvertimeReminderEvent(
                        user.getId(), user.getEmail(), user.getFullName(), deadline));
                attendance.setOvertimeReminderSent(true);
                attendanceRepository.save(attendance);
                actions++;
                log.info("Overtime reminder for {} (window closes {})", user.getEmail(), deadline);
            }
        }
        return actions;
    }

    @Override
    @Transactional
    public AttendanceResponse extendOvertime(String email) {
        Attendance attendance = getTodayAttendance(email);

        if (attendance.getStatus() != AttendanceStatus.WORKING
                || attendance.getOvertimeDeadline() == null) {
            throw new BadRequestException("You are not currently in an overtime window.");
        }

        // Extend from whichever is later — the current deadline or now — so a
        // just-missed window still yields a full fresh block.
        LocalDateTime base = DateTimeUtil.now().isAfter(attendance.getOvertimeDeadline())
                ? DateTimeUtil.now() : attendance.getOvertimeDeadline();
        attendance.setOvertimeDeadline(base.plusMinutes(overtimeWindowMinutes));
        attendance.setOvertimeReminderSent(false);

        log.info("Overtime extended for {} until {}", email, attendance.getOvertimeDeadline());
        return attendanceMapper.toAttendanceResponse(attendanceRepository.save(attendance));
    }

    /**
     * Closes the open break (if any) at the given end time: stores its
     * duration and accumulates it onto the attendance's total.
     * Shared by resumeWork and clockOut.
     */
    private void closeOpenBreak(Attendance attendance, LocalDateTime endTime) {
        workBreakRepository.findByAttendanceAndEndTimeIsNull(attendance)
                .ifPresent(workBreak -> {
                    long minutes = Duration.between(workBreak.getStartTime(), endTime).toMinutes();
                    workBreak.setEndTime(endTime);
                    workBreak.setDurationMinutes((int) Math.max(minutes, 0));
                    workBreakRepository.save(workBreak);

                    attendance.setTotalBreakMinutes(
                            attendance.getTotalBreakMinutes() + workBreak.getDurationMinutes());
                });
    }

    /**
     * Flags the first check-in of the day as a late arrival (and the day as a
     * half day) when it comes more than the grace period after the start time
     * the user declared in their work plan. Users without a plan requirement
     * (teachers, super admin) are never penalized here.
     */
    private void applyLateArrivalPenalty(User user, Attendance attendance,
                                         LocalDate today, LocalDateTime now) {
        if (!workPlanService.isPlanRequired(user)) {
            return;
        }
        workPlanService.getPlannedStartTime(user, today).ifPresent(plannedStart -> {
            LocalDateTime graceLimit = today.atTime(plannedStart)
                    .plusMinutes(LATE_GRACE_MINUTES);
            if (now.isAfter(graceLimit)) {
                long minutesLate = Duration
                        .between(today.atTime(plannedStart), now).toMinutes();
                attendance.setLateArrival(true);
                attendance.setHalfDay(true);
                eventPublisher.publishEvent(new LateArrivalEvent(
                        user.getId(), user.getEmail(), plannedStart, minutesLate));
                log.info("Late arrival for {}: planned {}, checked in {} min late — half day",
                        user.getEmail(), plannedStart, minutesLate);
            }
        });
    }

    /** Loads the caller's attendance record for today, or 404. */
    private Attendance getTodayAttendance(String email) {
        User user = findUserByEmail(email);
        LocalDate today = DateTimeUtil.today();
        return attendanceRepository.findByUserAndWorkDate(user, today)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "date", today));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
