package com.institute.workforce_tracking.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.request.SubmitWorkReportRequest;
import com.institute.workforce_tracking.dto.response.OpenWorkReportDayResponse;
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
import com.institute.workforce_tracking.entity.WorkPlan;
import com.institute.workforce_tracking.repository.AttendanceRepository;
import com.institute.workforce_tracking.repository.DeadlineExtensionRepository;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.repository.WorkPlanRepository;
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
    private final WorkPlanRepository workPlanRepository;

    /** Roles that must submit work reports — teachers exempt. */
    private static final List<Role> REPORTING_ROLES =
            List.of(Role.EMPLOYEE, Role.ADMIN, Role.SUPER_ADMIN);

    private static final int BASE_DEADLINE_HOURS = 24;

    /** How far back the "still owed" day list looks. Anything older is closed anyway. */
    private static final int OPEN_DAYS_LOOKBACK = 60;

    @Override
    @Transactional
    public WorkReportResponse submitReport(String email, SubmitWorkReportRequest request) {
        User user = findUserByEmail(email);
        Attendance attendance = resolveAttendance(user, request.workDate());

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
        report.setCheckInTime(attendance.getLoginTime());

        // Copy the day's declared work schedule onto the report, if one exists
        // (Super Admins are not required to file a plan).
        workPlanRepository.findByUserAndPlanDate(user, attendance.getWorkDate())
                .ifPresent(plan -> {
                    report.setPlannedStartTime(plan.getPlannedStartTime());
                    report.setPlannedEndTime(plan.getPlannedEndTime());
                    report.setPlannedWork(plan.getWorkDescription());
                });

        // Measured against the normal window, not the extended one: a report
        // can no longer arrive after the extended deadline at all, so comparing
        // with that would make the flag permanently false. Late now means
        // exactly "only an admin extension let this through".
        report.setSubmittedLate(report.getSubmittedAt().isAfter(baseDeadline(attendance)));

        // Lifts an absence the sweep applied before the extension was granted.
        // Granting normally clears it, so this is a safety net for the gap
        // between a sweep and the grant it races.
        if (attendance.isAbsentNoReport()) {
            attendance.setAbsentNoReport(false);
            attendanceRepository.save(attendance);
        }

        return workReportMapper.toWorkReportResponse(workReportRepository.save(report));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpenWorkReportDayResponse> getOpenReportDays(String email) {
        User user = findUserByEmail(email);
        LocalDateTime now = DateTimeUtil.now();

        return attendanceRepository
                .findByUserAndStatusAndWorkDateGreaterThanEqual(user, AttendanceStatus.CHECKED_OUT,
                        DateTimeUtil.today().minusDays(OPEN_DAYS_LOOKBACK))
                .stream()
                .filter(attendance -> !workReportRepository.existsByUserAndWorkDate(
                        user, attendance.getWorkDate()))
                // The same test resolveAttendance applies, so the picker can
                // never offer a day the submit would reject. A closed day
                // simply disappears until an admin extends it.
                .filter(attendance -> !now.isAfter(effectiveDeadline(user, attendance)))
                .sorted(Comparator.comparing(Attendance::getWorkDate).reversed())
                .map(attendance -> new OpenWorkReportDayResponse(
                        attendance.getWorkDate(),
                        attendance.getLogoutTime(),
                        effectiveDeadline(user, attendance),
                        extraHours(user, attendance.getWorkDate()),
                        attendance.isAbsentNoReport(),
                        now.isAfter(baseDeadline(attendance))))
                .toList();
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
    @Transactional(readOnly = true)
    public PagedResponse<WorkReportResponse> getReportsByDateRange(
            LocalDate from, LocalDate to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "workDate", "submittedAt"));
        Page<WorkReport> reports = workReportRepository.findByWorkDateBetween(from, to, pageable);
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

    /**
     * The checked-out day a submission applies to, once it is established that
     * the day may still be reported on.
     *
     * <p>With no {@code workDate} this is the most recent checkout, which is
     * how the form has always behaved. Whichever day is targeted, the window
     * is enforced the same way: past the deadline nothing can be filed, and
     * the only thing that reopens a lapsed day is an admin extension. A day
     * left unreported once its window closes stays marked absent — there is
     * deliberately no path for a user to clear that on their own.</p>
     */
    private Attendance resolveAttendance(User user, LocalDate workDate) {
        Attendance attendance = workDate == null
                ? latestCheckout(user).orElseThrow(() -> new BadRequestException(
                        "No checked-out attendance found. "
                                + "Check out first before submitting a report."))
                : attendanceRepository.findByUserAndWorkDate(user, workDate)
                        .filter(record -> record.getStatus() == AttendanceStatus.CHECKED_OUT)
                        .orElseThrow(() -> new BadRequestException(
                                "No checked-out attendance found for " + workDate + "."));

        LocalDateTime deadline = effectiveDeadline(user, attendance);
        if (DateTimeUtil.now().isAfter(deadline)) {
            throw new BadRequestException(closedWindowMessage(user, attendance, deadline));
        }
        return attendance;
    }

    /**
     * Why a submission was refused, phrased so the user knows whether asking
     * an admin is worth it: a day that has already been extended once needs a
     * further extension, not a first one.
     */
    private String closedWindowMessage(User user, Attendance attendance, LocalDateTime deadline) {
        boolean alreadyExtended = extraHours(user, attendance.getWorkDate()) > 0;
        return "The " + (alreadyExtended ? "extended " : "")
                + "reporting window for " + attendance.getWorkDate() + " closed on "
                + DateTimeUtil.formatDateTime(deadline) + ", so that day stays marked absent. "
                + "Only an admin can reopen it by "
                + (alreadyExtended ? "extending the deadline again." : "extending the deadline.");
    }

    /**
     * The user's most recent checked-out day — the default target of a
     * submission, and the one day the picker always offers.
     */
    private Optional<Attendance> latestCheckout(User user) {
        Pageable recentCheckout = PageRequest.of(0, 1, Sort.by("workDate").descending());
        return attendanceRepository
                .findByUserAndStatus(user, AttendanceStatus.CHECKED_OUT, recentCheckout)
                .getContent()
                .stream()
                .findFirst();
    }

    /** Checkout + 24h: the window everyone gets without asking anyone. */
    private LocalDateTime baseDeadline(Attendance attendance) {
        return attendance.getLogoutTime().plusHours(BASE_DEADLINE_HOURS);
    }

    /** Checkout + 24h, plus any admin-granted extension for that day. */
    private LocalDateTime effectiveDeadline(User user, Attendance attendance) {
        return baseDeadline(attendance).plusHours(extraHours(user, attendance.getWorkDate()));
    }

    /** Hours an admin added to this user's report deadline for a day, 0 if none. */
    private int extraHours(User user, LocalDate workDate) {
        return deadlineExtensionRepository
                .findByUserAndTypeAndTargetDate(user, DeadlineType.WORK_REPORT, workDate)
                .map(DeadlineExtension::getExtraHours)
                .orElse(0);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
