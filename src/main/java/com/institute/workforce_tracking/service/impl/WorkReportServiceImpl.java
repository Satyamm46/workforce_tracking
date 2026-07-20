package com.institute.workforce_tracking.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.request.SubmitWorkReportRequest;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.dto.response.WorkReportResponse;
import com.institute.workforce_tracking.entity.Attendance;
import com.institute.workforce_tracking.entity.DeadlineExtension;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.entity.WorkReport;
import com.institute.workforce_tracking.enums.AttendanceStatus;
import com.institute.workforce_tracking.enums.DeadlineType;
import com.institute.workforce_tracking.enums.Role;
import com.institute.workforce_tracking.exception.BadRequestException;
import com.institute.workforce_tracking.exception.ResourceNotFoundException;
import com.institute.workforce_tracking.mapper.WorkReportMapper;
import com.institute.workforce_tracking.repository.AttendanceRepository;
import com.institute.workforce_tracking.repository.DeadlineExtensionRepository;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.repository.WorkReportRepository;
import com.institute.workforce_tracking.service.WorkReportService;
import com.institute.workforce_tracking.util.DateTimeUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkReportServiceImpl implements WorkReportService {

    private final WorkReportRepository workReportRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final WorkReportMapper workReportMapper;
    private final DeadlineExtensionRepository deadlineExtensionRepository;

    /** Roles that must submit work reports — teachers exempt. */
    private static final List<Role> REPORTING_ROLES =
            List.of(Role.EMPLOYEE, Role.ADMIN, Role.SUPER_ADMIN);

    private static final int BASE_DEADLINE_HOURS = 24;

    @Override
    @Transactional
    public WorkReportResponse submitReport(String email, SubmitWorkReportRequest request) {
        User user = findUserByEmail(email);

        // Find the most recent checked-out attendance for this user.
        Pageable recentCheckout = PageRequest.of(0, 1, Sort.by("workDate").descending());
        List<Attendance> recent = attendanceRepository
                .findByUserAndStatus(user, AttendanceStatus.CHECKED_OUT, recentCheckout)
                .getContent();

        if (recent.isEmpty()) {
            throw new BadRequestException(
                    "No checked-out attendance found. Check out first before submitting a report.");
        }

        Attendance attendance = recent.get(0);
        if (workReportRepository.existsByUserAndWorkDate(user, attendance.getWorkDate())) {
            throw new BadRequestException(
                    "You have already submitted a report for " + attendance.getWorkDate() + ".");
        }

        WorkReport report = new WorkReport();
        report.setUser(user);
        report.setWorkDate(attendance.getWorkDate());
        report.setReportText(request.reportText());
        report.setSubmittedAt(DateTimeUtil.now());
        report.setCheckoutTime(attendance.getLogoutTime());
        report.setSubmittedLate(report.getSubmittedAt()
                .isAfter(effectiveDeadline(user, attendance)));

        // A late-but-within-extension submission also lifts an absence the
        // sweep may already have applied.
        if (attendance.isAbsentNoReport()) {
            attendance.setAbsentNoReport(false);
            attendanceRepository.save(attendance);
        }

        return workReportMapper.toWorkReportResponse(workReportRepository.save(report));
    }

    @Override
    @Transactional(readOnly = true)
    public WorkReportResponse getMyReportForDay(String email, LocalDate date) {
        User user = findUserByEmail(email);
        WorkReport report = workReportRepository.findByUserAndWorkDate(user, date)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No report found for " + date + "."));
        return workReportMapper.toWorkReportResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WorkReportResponse> getMyReports(String email, int page, int size) {
        User user = findUserByEmail(email);
        Pageable pageable = PageRequest.of(page, size, Sort.by("workDate").descending());
        Page<WorkReport> reports = workReportRepository.findByUser(user, pageable);
        return PagedResponse.of(reports, workReportMapper::toWorkReportResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WorkReportResponse> getReportsByDate(LocalDate date, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt"));
        Page<WorkReport> reports = workReportRepository.findByWorkDate(date, pageable);
        return PagedResponse.of(reports, workReportMapper::toWorkReportResponse);
    }

    @Override
    @Transactional
    public int markAbsentForMissingReports() {
        LocalDateTime now = DateTimeUtil.now();
        LocalDateTime baseCutoff = now.minusHours(BASE_DEADLINE_HOURS);

        // Checked-out attendances past the base window, for reporting roles,
        // still unreported and not already marked. Each candidate's real
        // deadline (base + any admin extension) is then checked individually.
        List<Attendance> overdueCheckouts = attendanceRepository
                .findByStatusAndLogoutTimeLessThanEqual(AttendanceStatus.CHECKED_OUT, baseCutoff)
                .stream()
                .filter(attendance -> !attendance.isAbsentNoReport())
                .filter(attendance -> REPORTING_ROLES.contains(attendance.getUser().getRole()))
                .filter(attendance -> !workReportRepository.existsByUserAndWorkDate(
                        attendance.getUser(), attendance.getWorkDate()))
                .filter(attendance -> now.isAfter(
                        effectiveDeadline(attendance.getUser(), attendance)))
                .toList();

        // Mark absent via the reversible flag — worked minutes stay intact so
        // a later-granted extension can restore the day.
        overdueCheckouts.forEach(attendance -> {
            attendance.setAbsentNoReport(true);
            log.info("Marked user {} absent for {} — no report submitted within deadline",
                    attendance.getUser().getEmail(), attendance.getWorkDate());
        });

        attendanceRepository.saveAll(overdueCheckouts);
        return overdueCheckouts.size();
    }

    /** Checkout + 24h, plus any admin-granted extension for that day. */
    private LocalDateTime effectiveDeadline(User user, Attendance attendance) {
        int extraHours = deadlineExtensionRepository
                .findByUserAndTypeAndTargetDate(user, DeadlineType.WORK_REPORT,
                        attendance.getWorkDate())
                .map(DeadlineExtension::getExtraHours)
                .orElse(0);
        return attendance.getLogoutTime().plusHours(BASE_DEADLINE_HOURS + extraHours);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
