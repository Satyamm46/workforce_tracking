package com.institute.workforce_tracking.service.impl;

import java.time.Duration;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.response.AttendanceReportRow;
import com.institute.workforce_tracking.dto.response.TeacherReportRow;
import com.institute.workforce_tracking.entity.Lecture;
import com.institute.workforce_tracking.enums.LectureStatus;
import com.institute.workforce_tracking.exception.BadRequestException;
import com.institute.workforce_tracking.repository.AttendanceRepository;
import com.institute.workforce_tracking.repository.LectureRepository;
import com.institute.workforce_tracking.service.ReportService;

/**
 * Default implementation of {@link ReportService}.
 *
 * <p>The attendance report is aggregated by the database (pure column sums);
 * the teaching report is aggregated in Java because teaching minutes require
 * LocalTime arithmetic that already lives in the domain layer.</p>
 */
@Service
public class ReportServiceImpl implements ReportService {

    private final AttendanceRepository attendanceRepository;
    private final LectureRepository lectureRepository;

    public ReportServiceImpl(AttendanceRepository attendanceRepository,
                             LectureRepository lectureRepository) {
        this.attendanceRepository = attendanceRepository;
        this.lectureRepository = lectureRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceReportRow> getMonthlyAttendanceReport(int year, int month) {
        YearMonth period = toPeriod(year, month);
        return attendanceRepository.buildAttendanceReport(
                period.atDay(1), period.atEndOfMonth());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherReportRow> getMonthlyTeachingReport(int year, int month) {
        YearMonth period = toPeriod(year, month);

        List<Lecture> completed = lectureRepository.findByStatusAndLectureDateBetween(
                LectureStatus.COMPLETED, period.atDay(1), period.atEndOfMonth());

        // Group by teacher ID (never by entity — entities use identity equality).
        Map<Long, List<Lecture>> byTeacher = completed.stream()
                .collect(Collectors.groupingBy(lecture -> lecture.getTeacher().getId()));

        return byTeacher.values().stream()
                .map(this::summarizeTeacher)
                .sorted(Comparator.comparing(TeacherReportRow::fullName))
                .toList();
    }

    /** Folds one teacher's completed lectures into a report row. */
    private TeacherReportRow summarizeTeacher(List<Lecture> lectures) {
        Lecture first = lectures.get(0);
        long teachingMinutes = 0;
        long extensionMinutes = 0;

        for (Lecture lecture : lectures) {
            teachingMinutes += Duration
                    .between(lecture.getStartTime(), lecture.getEffectiveEndTime())
                    .toMinutes();
            extensionMinutes += lecture.getExtendedMinutes();
        }

        return new TeacherReportRow(
                first.getTeacher().getId(),
                first.getTeacher().getFullName(),
                lectures.size(),
                teachingMinutes,
                extensionMinutes);
    }

    /** Validates and converts year/month input into a period. */
    private YearMonth toPeriod(int year, int month) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12.");
        }
        return YearMonth.of(year, month);
    }
}
