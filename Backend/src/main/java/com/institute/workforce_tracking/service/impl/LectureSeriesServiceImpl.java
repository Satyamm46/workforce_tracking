package com.institute.workforce_tracking.service.impl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.request.CreateLectureSeriesRequest;
import com.institute.workforce_tracking.dto.response.LectureSeriesResponse;
import com.institute.workforce_tracking.entity.Lecture;
import com.institute.workforce_tracking.entity.LectureSeries;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.enums.LectureStatus;
import com.institute.workforce_tracking.enums.RecurrenceFrequency;
import com.institute.workforce_tracking.exception.BadRequestException;
import com.institute.workforce_tracking.exception.ResourceNotFoundException;
import com.institute.workforce_tracking.mapper.LectureSeriesMapper;
import com.institute.workforce_tracking.repository.LectureRepository;
import com.institute.workforce_tracking.repository.LectureSeriesRepository;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.service.LectureSeriesService;
import com.institute.workforce_tracking.util.DateTimeUtil;

/**
 * Default implementation of {@link LectureSeriesService}.
 *
 * <p>Occurrences are materialised as ordinary {@link Lecture} rows on a
 * rolling {@value #HORIZON_DAYS}-day horizon: create fills the horizon
 * immediately, and the nightly sweep tops every active series back up as the
 * window slides forward. {@code materializedThrough} is the watermark that
 * keeps both incremental and idempotent.</p>
 *
 * <p>Dates that clash with an existing lecture are <em>skipped, not fatal</em>:
 * a term-long series will inevitably collide with a one-off booking at some
 * point, and failing the whole series over one clash would be hostile. The
 * skipped dates are reported back on create so the teacher can react.</p>
 */
@Service
public class LectureSeriesServiceImpl implements LectureSeriesService {

    /** How far ahead occurrences are kept materialised (~8 weeks). */
    private static final int HORIZON_DAYS = 56;

    /** How far out an end date may reach — sanity cap, not a business rule. */
    private static final int MAX_END_DATE_MONTHS = 24;

    private final LectureSeriesRepository lectureSeriesRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;
    private final LectureSeriesMapper lectureSeriesMapper;

    public LectureSeriesServiceImpl(LectureSeriesRepository lectureSeriesRepository,
                                    LectureRepository lectureRepository,
                                    UserRepository userRepository,
                                    LectureSeriesMapper lectureSeriesMapper) {
        this.lectureSeriesRepository = lectureSeriesRepository;
        this.lectureRepository = lectureRepository;
        this.userRepository = userRepository;
        this.lectureSeriesMapper = lectureSeriesMapper;
    }

    @Override
    @Transactional
    public LectureSeriesResponse createSeries(String teacherEmail,
                                              CreateLectureSeriesRequest request) {
        User teacher = findUserByEmail(teacherEmail);
        validate(request);

        LectureSeries series = new LectureSeries();
        series.setTeacher(teacher);
        series.setSubject(request.subject().trim());
        series.setClassName(request.className().trim());
        series.setBatch(normalizeBatch(request.batch()));
        series.setStartTime(request.startTime());
        series.setEndTime(request.endTime());
        series.setFrequency(request.frequency());
        series.setStartDate(request.startDate());
        series.setEndDate(request.endDate());
        series.setActive(true);
        if (request.frequency() == RecurrenceFrequency.WEEKLY) {
            series.setWeekdays(request.weekdays());
        } else {
            // The monthly anchor comes from the start date, not a separate
            // field — "starts on the 15th" and "repeats on the 15th" are the
            // same statement.
            series.setDayOfMonth(request.startDate().getDayOfMonth());
        }
        series = lectureSeriesRepository.save(series);

        List<String> skipped = new ArrayList<>();
        int created = materialise(series, skipped);

        return lectureSeriesMapper.toResponse(series, created, skipped);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureSeriesResponse> getMySeries(String teacherEmail) {
        User teacher = findUserByEmail(teacherEmail);
        return lectureSeriesRepository
                .findByTeacherAndActiveTrueOrderByStartTimeAsc(teacher).stream()
                .map(lectureSeriesMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public LectureSeriesResponse stopSeries(String teacherEmail, Long seriesId) {
        User teacher = findUserByEmail(teacherEmail);
        LectureSeries series = lectureSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture series", "id", seriesId));
        if (!series.getTeacher().getId().equals(teacher.getId())) {
            // Other teachers' series are treated as nonexistent, matching
            // findOwnedLecture in LectureServiceImpl.
            throw new ResourceNotFoundException("Lecture series", "id", seriesId);
        }
        if (!series.isActive()) {
            throw new BadRequestException("This series has already been stopped.");
        }

        series.setActive(false);
        lectureSeriesRepository.save(series);

        // Future occurrences are cancelled; past ones stay on record so
        // reports remain honest. Today's occurrence counts as future only if
        // still SCHEDULED — a LIVE or COMPLETED class is untouched by design.
        List<Lecture> upcoming = lectureRepository
                .findBySeriesAndLectureDateGreaterThanEqualAndStatus(
                        series, DateTimeUtil.today(), LectureStatus.SCHEDULED);
        upcoming.forEach(lecture -> lecture.setStatus(LectureStatus.CANCELLED));
        lectureRepository.saveAll(upcoming);

        return lectureSeriesMapper.toResponse(series);
    }

    @Override
    @Transactional
    public int materialiseUpcoming() {
        int created = 0;
        for (LectureSeries series : lectureSeriesRepository.findByActiveTrue()) {
            created += materialise(series, new ArrayList<>());
        }
        return created;
    }

    /**
     * Generates this series' missing occurrences up to the rolling horizon,
     * adding any conflict-skipped dates (ISO format) to {@code skipped}.
     * Updates the watermark; returns how many lectures were created.
     */
    private int materialise(LectureSeries series, List<String> skipped) {
        LocalDate today = DateTimeUtil.today();

        LocalDate from = series.getStartDate();
        if (from.isBefore(today)) {
            from = today;
        }
        if (series.getMaterializedThrough() != null
                && !series.getMaterializedThrough().isBefore(from)) {
            from = series.getMaterializedThrough().plusDays(1);
        }

        LocalDate to = today.plusDays(HORIZON_DAYS);
        if (series.getEndDate() != null && series.getEndDate().isBefore(to)) {
            to = series.getEndDate();
        }
        if (from.isAfter(to)) {
            return 0; // already topped up, or the series has run out
        }

        int created = 0;
        for (LocalDate date : occurrenceDates(series, from, to)) {
            if (lectureRepository.existsConflictingLecture(
                    series.getTeacher(), date, series.getStartTime(), series.getEndTime())) {
                skipped.add(date.toString());
                continue;
            }
            Lecture lecture = new Lecture();
            lecture.setTeacher(series.getTeacher());
            lecture.setSeries(series);
            lecture.setSubject(series.getSubject());
            lecture.setClassName(series.getClassName());
            lecture.setBatch(series.getBatch());
            lecture.setLectureDate(date);
            lecture.setStartTime(series.getStartTime());
            lecture.setEndTime(series.getEndTime());
            lecture.setStatus(LectureStatus.SCHEDULED);
            lectureRepository.save(lecture);
            created++;
        }

        series.setMaterializedThrough(to);
        lectureSeriesRepository.save(series);
        return created;
    }

    /** The dates in [from, to] this series lands on, in order. */
    private List<LocalDate> occurrenceDates(LectureSeries series, LocalDate from, LocalDate to) {
        List<LocalDate> dates = new ArrayList<>();
        if (series.getFrequency() == RecurrenceFrequency.WEEKLY) {
            var weekdays = series.getWeekdays();
            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                if (weekdays.contains(date.getDayOfWeek())) {
                    dates.add(date);
                }
            }
        } else {
            // Clamp to each month's length: a series on the 31st runs on the
            // 30th — or the 28th — in shorter months rather than skipping them.
            int day = series.getDayOfMonth();
            YearMonth month = YearMonth.from(from);
            YearMonth last = YearMonth.from(to);
            while (!month.isAfter(last)) {
                LocalDate date = month.atDay(Math.min(day, month.lengthOfMonth()));
                if (!date.isBefore(from) && !date.isAfter(to)) {
                    dates.add(date);
                }
                month = month.plusMonths(1);
            }
        }
        return dates;
    }

    private void validate(CreateLectureSeriesRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("End time must be after start time.");
        }
        LocalDate today = DateTimeUtil.today();
        if (request.startDate().isBefore(today)) {
            throw new BadRequestException("Start date cannot be in the past.");
        }
        if (request.frequency() == RecurrenceFrequency.WEEKLY
                && (request.weekdays() == null || request.weekdays().isEmpty())) {
            throw new BadRequestException("Pick at least one weekday for a weekly repeat.");
        }
        if (request.endDate() != null) {
            if (request.endDate().isBefore(request.startDate())) {
                throw new BadRequestException("End date cannot be before the start date.");
            }
            if (request.endDate().isAfter(today.plusMonths(MAX_END_DATE_MONTHS))) {
                throw new BadRequestException("End date cannot be more than "
                        + MAX_END_DATE_MONTHS + " months away.");
            }
        }
    }

    /** Treats blank or empty batch input as "no batch" (stored as null). */
    private String normalizeBatch(String batch) {
        if (batch == null || batch.isBlank()) {
            return null;
        }
        return batch.trim();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
