package com.institute.workforce_tracking.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.response.DashboardMemberResponse;
import com.institute.workforce_tracking.dto.response.DashboardStatsResponse;
import com.institute.workforce_tracking.entity.Attendance;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.enums.AttendanceStatus;
import com.institute.workforce_tracking.enums.DashboardGroup;
import com.institute.workforce_tracking.enums.LectureStatus;
import com.institute.workforce_tracking.repository.AttendanceRepository;
import com.institute.workforce_tracking.repository.LectureRepository;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.repository.WorkBreakRepository;
import com.institute.workforce_tracking.service.DashboardService;
import com.institute.workforce_tracking.util.DateTimeUtil;

/**
 * Default implementation of {@link DashboardService}: composes count queries
 * across users, attendance, and lectures into one snapshot, and expands any
 * single count into the people behind it.
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final LectureRepository lectureRepository;
    private final WorkBreakRepository workBreakRepository;

    public DashboardServiceImpl(UserRepository userRepository,
                                AttendanceRepository attendanceRepository,
                                LectureRepository lectureRepository,
                                WorkBreakRepository workBreakRepository) {
        this.userRepository = userRepository;
        this.attendanceRepository = attendanceRepository;
        this.lectureRepository = lectureRepository;
        this.workBreakRepository = workBreakRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        LocalDate today = DateTimeUtil.today();

        long total = userRepository.countByEnabledTrue();

        // One grouped query instead of four separate counts over the same rows.
        // This runs every 15 seconds for the life of the app, so the saving is
        // three round trips and three table scans per tick.
        Map<AttendanceStatus, Long> counts = new EnumMap<>(AttendanceStatus.class);
        for (Object[] row : attendanceRepository.countByWorkDateGroupedByStatus(today)) {
            counts.put((AttendanceStatus) row[0], (Long) row[1]);
        }

        long working = counts.getOrDefault(AttendanceStatus.WORKING, 0L);
        long onBreak = counts.getOrDefault(AttendanceStatus.ON_BREAK, 0L);
        long checkedOut = counts.getOrDefault(AttendanceStatus.CHECKED_OUT, 0L);
        long onLeave = counts.getOrDefault(AttendanceStatus.ON_LEAVE, 0L);
        long liveLectures = lectureRepository.countByStatus(LectureStatus.LIVE);

        long present = working + onBreak + checkedOut + onLeave;
        long absent = Math.max(total - present, 0);

        return new DashboardStatsResponse(
                total,
                working + onBreak,
                working,
                onBreak,
                checkedOut,
                onLeave,
                absent,
                liveLectures
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardMemberResponse> getMembers(DashboardGroup group) {
        return switch (group) {
            case TOTAL -> everyone();
            case ONLINE -> byStatus(AttendanceStatus.WORKING, AttendanceStatus.ON_BREAK);
            case WORKING -> byStatus(AttendanceStatus.WORKING);
            case ON_BREAK -> byStatus(AttendanceStatus.ON_BREAK);
            case CHECKED_OUT -> byStatus(AttendanceStatus.CHECKED_OUT);
            case ON_LEAVE -> byStatus(AttendanceStatus.ON_LEAVE);
            case IN_LECTURE -> teachersInLecture();
            case ABSENT -> absentToday();
        };
    }

    /**
     * Everyone active, each annotated with what they are doing today. The
     * whole-roster view is the one place where mixed statuses are the point.
     */
    private List<DashboardMemberResponse> everyone() {
        List<Attendance> todays = attendanceRepository.findByWorkDateAndStatusIn(
                DateTimeUtil.today(), List.of(AttendanceStatus.values()));
        Map<Long, Attendance> byUser = new HashMap<>();
        todays.forEach(attendance -> byUser.put(attendance.getUser().getId(), attendance));
        Map<Long, LocalDateTime> breakStarts = openBreakStarts(todays);

        return userRepository.findByEnabledTrueOrderByFullName().stream()
                .map(user -> {
                    Attendance attendance = byUser.get(user.getId());
                    return attendance == null
                            ? new DashboardMemberResponse(
                                    user.getId(), user.getFullName(), user.getRole(),
                                    "No attendance today", null)
                            : describe(attendance, breakStarts);
                })
                .toList();
    }

    /** The people whose attendance today sits in any of the given statuses. */
    private List<DashboardMemberResponse> byStatus(AttendanceStatus... statuses) {
        List<Attendance> matching = attendanceRepository.findByWorkDateAndStatusIn(
                DateTimeUtil.today(), List.of(statuses));
        Map<Long, LocalDateTime> breakStarts = openBreakStarts(matching);

        return matching.stream()
                .map(attendance -> describe(attendance, breakStarts))
                .sorted(Comparator.comparing(DashboardMemberResponse::fullName,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /** Teachers whose lecture is in progress, labelled with what they teach. */
    private List<DashboardMemberResponse> teachersInLecture() {
        return lectureRepository.findByStatus(LectureStatus.LIVE).stream()
                .map(lecture -> {
                    User teacher = lecture.getTeacher();
                    return new DashboardMemberResponse(
                            teacher.getId(),
                            teacher.getFullName(),
                            teacher.getRole(),
                            lecture.getSubject() + " · " + lecture.getClassName(),
                            lecture.getLectureDate().atTime(lecture.getStartTime()));
                })
                .sorted(Comparator.comparing(DashboardMemberResponse::fullName,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Active accounts with no attendance row today. Absence is the absence of
     * a record, so it is a set difference rather than a query.
     */
    private List<DashboardMemberResponse> absentToday() {
        Set<Long> accountedFor =
                attendanceRepository.findUserIdsByWorkDate(DateTimeUtil.today());

        return userRepository.findByEnabledTrueOrderByFullName().stream()
                .filter(user -> !accountedFor.contains(user.getId()))
                .map(user -> new DashboardMemberResponse(
                        user.getId(), user.getFullName(), user.getRole(),
                        "No attendance today", null))
                .toList();
    }

    /**
     * The moment each person's current state began: when the break started for
     * anyone on one, otherwise check-in or checkout. That timestamp is what
     * turns "on a break" into something an admin can act on.
     */
    private DashboardMemberResponse describe(Attendance attendance,
                                             Map<Long, LocalDateTime> breakStarts) {
        User user = attendance.getUser();
        String status = switch (attendance.getStatus()) {
            case WORKING -> "Working";
            case ON_BREAK -> "On break";
            case CHECKED_OUT -> "Checked out";
            case ON_LEAVE -> "On approved leave";
        };
        LocalDateTime since = switch (attendance.getStatus()) {
            case WORKING -> attendance.getLoginTime();
            case ON_BREAK -> breakStarts.getOrDefault(attendance.getId(),
                    attendance.getLoginTime());
            case CHECKED_OUT -> attendance.getLogoutTime();
            case ON_LEAVE -> null;
        };
        return new DashboardMemberResponse(
                user.getId(), user.getFullName(), user.getRole(), status, since);
    }

    /**
     * Start time of the open break on each of these days, keyed by attendance
     * id. Fetched in one query rather than per person.
     */
    private Map<Long, LocalDateTime> openBreakStarts(List<Attendance> attendances) {
        List<Attendance> onBreak = attendances.stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.ON_BREAK)
                .toList();
        if (onBreak.isEmpty()) {
            return Map.of();
        }

        Map<Long, LocalDateTime> starts = new HashMap<>();
        workBreakRepository.findByAttendanceInAndEndTimeIsNull(onBreak).forEach(
                workBreak -> starts.put(workBreak.getAttendance().getId(),
                        workBreak.getStartTime()));
        return starts;
    }
}
