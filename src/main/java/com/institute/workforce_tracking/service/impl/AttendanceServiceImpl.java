package com.institute.workforce_tracking.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
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
import com.institute.workforce_tracking.enums.AttendanceStatus;
import com.institute.workforce_tracking.event.UserLoggedInEvent;
import com.institute.workforce_tracking.exception.BadRequestException;
import com.institute.workforce_tracking.exception.ResourceNotFoundException;
import com.institute.workforce_tracking.mapper.AttendanceMapper;
import com.institute.workforce_tracking.repository.AttendanceRepository;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.repository.WorkBreakRepository;
import com.institute.workforce_tracking.service.AttendanceService;
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

    private final AttendanceRepository attendanceRepository;
    private final WorkBreakRepository workBreakRepository;
    private final UserRepository userRepository;
    private final AttendanceMapper attendanceMapper;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 WorkBreakRepository workBreakRepository,
                                 UserRepository userRepository,
                                 AttendanceMapper attendanceMapper) {
        this.attendanceRepository = attendanceRepository;
        this.workBreakRepository = workBreakRepository;
        this.userRepository = userRepository;
        this.attendanceMapper = attendanceMapper;
    }

    @EventListener
    public void onUserLoggedIn(UserLoggedInEvent event) {
        try {
            LocalDate today = DateTimeUtil.today();
            User user = userRepository.getReferenceById(event.userId());

            if (attendanceRepository.findByUserAndWorkDate(user, today).isPresent()) {
                return; // already clocked in today — idempotent
            }

            Attendance attendance = new Attendance();
            attendance.setUser(user);
            attendance.setWorkDate(today);
            attendance.setLoginTime(DateTimeUtil.now());
            attendance.setStatus(AttendanceStatus.WORKING);
            attendanceRepository.save(attendance);

            log.info("Clock-in recorded for user {} on {}", event.userId(), today);
        } catch (Exception ex) {
            log.error("Failed to record clock-in for user {}", event.userId(), ex);
        }
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
