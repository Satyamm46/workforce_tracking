package com.institute.workforce_tracking.service.impl;

import java.time.LocalDate;

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
        long working = attendanceRepository.countByWorkDateAndStatus(today, AttendanceStatus.WORKING);
        long onBreak = attendanceRepository.countByWorkDateAndStatus(today, AttendanceStatus.ON_BREAK);
        long checkedOut = attendanceRepository.countByWorkDateAndStatus(today, AttendanceStatus.CHECKED_OUT);
        long onLeave = attendanceRepository.countByWorkDateAndStatus(today, AttendanceStatus.ON_LEAVE);
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
