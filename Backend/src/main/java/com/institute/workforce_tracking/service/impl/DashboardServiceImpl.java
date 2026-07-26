package com.institute.workforce_tracking.service.impl;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.response.DashboardStatsResponse;
import com.institute.workforce_tracking.enums.AttendanceStatus;
import com.institute.workforce_tracking.enums.LectureStatus;
import com.institute.workforce_tracking.repository.AttendanceRepository;
import com.institute.workforce_tracking.repository.LectureRepository;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.service.DashboardService;
import com.institute.workforce_tracking.util.DateTimeUtil;

/**
 * Default implementation of {@link DashboardService}: composes count queries
 * across users, attendance, and lectures into one snapshot.
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final LectureRepository lectureRepository;

    public DashboardServiceImpl(UserRepository userRepository,
                                AttendanceRepository attendanceRepository,
                                LectureRepository lectureRepository) {
        this.userRepository = userRepository;
        this.attendanceRepository = attendanceRepository;
        this.lectureRepository = lectureRepository;
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
}
