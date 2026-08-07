package com.institute.workforce_tracking.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.request.ExtendLectureRequest;
import com.institute.workforce_tracking.dto.request.RescheduleLectureRequest;
import com.institute.workforce_tracking.dto.request.ScheduleLectureRequest;
import com.institute.workforce_tracking.dto.response.LectureResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.entity.Lecture;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.enums.LectureStatus;
import com.institute.workforce_tracking.event.LectureEndingSoonEvent;
import com.institute.workforce_tracking.event.LectureMissedEvent;
import com.institute.workforce_tracking.event.LectureStartingSoonEvent;
import com.institute.workforce_tracking.event.LecturesTomorrowEvent;
import com.institute.workforce_tracking.exception.BadRequestException;
import com.institute.workforce_tracking.exception.ResourceNotFoundException;
import com.institute.workforce_tracking.mapper.LectureMapper;
import com.institute.workforce_tracking.repository.LectureRepository;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.service.LectureService;
import com.institute.workforce_tracking.util.DateTimeUtil;
import com.institute.workforce_tracking.util.PageUtils;

/**
 * Default implementation of {@link LectureService}.
 *
 * <p>Covers the teacher-facing operations (schedule, cancel, end, extend),
 * the admin day view, and the three time-driven sweep operations invoked by
 * the lecture status scheduler.</p>
 */
@Service
public class LectureServiceImpl implements LectureService {

    private static final int MAX_EXTENSION_MINUTES = 30;
    private static final int REMINDER_WINDOW_MINUTES = 5;
    private static final int EARLY_START_ALLOWANCE_MINUTES = 10;

    /**
     * Widest calendar range a single request may ask for — two months plus
     * change, enough for any month view with padding, small enough that the
     * unpaged payload stays bounded.
     */
    private static final int MAX_CALENDAR_RANGE_DAYS = 62;

    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;
    private final LectureMapper lectureMapper;
    private final ApplicationEventPublisher eventPublisher;

    public LectureServiceImpl(LectureRepository lectureRepository,
                              UserRepository userRepository,
                              LectureMapper lectureMapper,
                              ApplicationEventPublisher eventPublisher) {
        this.lectureRepository = lectureRepository;
        this.userRepository = userRepository;
        this.lectureMapper = lectureMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public LectureResponse scheduleLecture(String teacherEmail, ScheduleLectureRequest request) {
        User teacher = findUserByEmail(teacherEmail);

        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("End time must be after start time.");
        }
        if (request.lectureDate().isEqual(DateTimeUtil.today())
                && request.startTime().isBefore(DateTimeUtil.now().toLocalTime())) {
            throw new BadRequestException("Start time has already passed for today.");
        }
        if (lectureRepository.existsConflictingLecture(
                teacher, request.lectureDate(), request.startTime(), request.endTime())) {
            throw new BadRequestException(
                    "This time overlaps another of your scheduled lectures on that day.");
        }

        Lecture lecture = new Lecture();
        lecture.setTeacher(teacher);
        lecture.setSubject(request.subject().trim());
        lecture.setClassName(request.className().trim());
        lecture.setBatch(normalizeBatch(request.batch()));
        lecture.setLectureDate(request.lectureDate());
        lecture.setStartTime(request.startTime());
        lecture.setEndTime(request.endTime());
        lecture.setStatus(LectureStatus.SCHEDULED);

        return lectureMapper.toLectureResponse(lectureRepository.save(lecture));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<LectureResponse> getMyUpcomingLectures(
            String teacherEmail, int page, int size) {

        User teacher = findUserByEmail(teacherEmail);
        Pageable pageable = PageRequest.of(PageUtils.safePage(page), PageUtils.safeSize(size),
                Sort.by(Sort.Direction.ASC, "lectureDate", "startTime"));

        Page<LectureResponse> result = lectureRepository
                .findByTeacherAndLectureDateGreaterThanEqual(teacher, DateTimeUtil.today(), pageable)
                .map(lectureMapper::toLectureResponse);

        return PagedResponse.from(result);
    }

    @Override
    @Transactional
    public LectureResponse cancelLecture(String teacherEmail, Long lectureId) {
        Lecture lecture = findOwnedLecture(teacherEmail, lectureId);
        if (lecture.getStatus() != LectureStatus.SCHEDULED) {
            throw new BadRequestException("Only scheduled lectures can be cancelled.");
        }
        lecture.setStatus(LectureStatus.CANCELLED);
        return lectureMapper.toLectureResponse(lectureRepository.save(lecture));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<LectureResponse> getLecturesByDate(LocalDate date, int page, int size) {
        Pageable pageable = PageRequest.of(PageUtils.safePage(page), PageUtils.safeSize(size),
                Sort.by(Sort.Direction.ASC, "startTime"));

        Page<LectureResponse> result = lectureRepository.findByLectureDate(date, pageable)
                .map(lectureMapper::toLectureResponse);

        return PagedResponse.from(result);
    }

    @Override
    @Transactional
    public LectureResponse endLecture(String teacherEmail, Long lectureId) {
        Lecture lecture = findOwnedLecture(teacherEmail, lectureId);
        if (lecture.getStatus() != LectureStatus.LIVE) {
            throw new BadRequestException("Only live lectures can be ended.");
        }
        lecture.setStatus(LectureStatus.COMPLETED);
        return lectureMapper.toLectureResponse(lectureRepository.save(lecture));
    }

    @Override
    @Transactional
    public LectureResponse startLecture(String teacherEmail, Long lectureId) {
        Lecture lecture = findOwnedLecture(teacherEmail, lectureId);
        if (lecture.getStatus() != LectureStatus.SCHEDULED) {
            throw new BadRequestException("Only scheduled lectures can be started.");
        }
        if (!lecture.getLectureDate().isEqual(DateTimeUtil.today())) {
            throw new BadRequestException("A lecture can only be started on its scheduled day.");
        }

        LocalTime now = DateTimeUtil.now().toLocalTime();
        if (now.isBefore(lecture.getStartTime().minusMinutes(EARLY_START_ALLOWANCE_MINUTES))) {
            throw new BadRequestException("Too early — you can start up to "
                    + EARLY_START_ALLOWANCE_MINUTES + " minutes before the scheduled time.");
        }

        // A late start shifts the session: the planned length is preserved
        // from this moment, so the effective end recalculates automatically.
        lecture.setActualStartTime(now);
        lecture.setStatus(LectureStatus.LIVE);
        return lectureMapper.toLectureResponse(lectureRepository.save(lecture));
    }

    @Override
    @Transactional
    public LectureResponse extendLecture(String teacherEmail, Long lectureId,
                                         ExtendLectureRequest request) {
        Lecture lecture = findOwnedLecture(teacherEmail, lectureId);
        if (lecture.getStatus() != LectureStatus.LIVE) {
            throw new BadRequestException("Only live lectures can be extended.");
        }
        int newTotal = lecture.getExtendedMinutes() + request.minutes();
        if (newTotal > MAX_EXTENSION_MINUTES) {
            throw new BadRequestException("Total extension cannot exceed "
                    + MAX_EXTENSION_MINUTES + " minutes; "
                    + (MAX_EXTENSION_MINUTES - lecture.getExtendedMinutes())
                    + " minute(s) remaining.");
        }
        lecture.setExtendedMinutes(newTotal);
        lecture.setReminderSent(false); // an extended lecture earns a fresh reminder
        return lectureMapper.toLectureResponse(lectureRepository.save(lecture));
    }

    @Override
    @Transactional
    public LectureResponse rescheduleLecture(String teacherEmail, Long lectureId,
                                             RescheduleLectureRequest request) {
        Lecture original = findOwnedLecture(teacherEmail, lectureId);
        if (original.getStatus() != LectureStatus.MISSED
                && original.getStatus() != LectureStatus.CANCELLED) {
            throw new BadRequestException("Only missed or cancelled lectures can be rescheduled.");
        }

        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("End time must be after start time.");
        }
        if (request.lectureDate().isEqual(DateTimeUtil.today())
                && request.startTime().isBefore(DateTimeUtil.now().toLocalTime())) {
            throw new BadRequestException("Start time has already passed for today.");
        }
        if (lectureRepository.existsConflictingLecture(
                original.getTeacher(), request.lectureDate(),
                request.startTime(), request.endTime())) {
            throw new BadRequestException(
                    "This time overlaps another of your scheduled lectures on that day.");
        }

        // A fresh lecture carries the session forward; the original stays on
        // record as missed/cancelled so reports remain honest.
        Lecture rescheduled = new Lecture();
        rescheduled.setTeacher(original.getTeacher());
        rescheduled.setSubject(original.getSubject());
        rescheduled.setClassName(original.getClassName());
        rescheduled.setBatch(original.getBatch());
        rescheduled.setLectureDate(request.lectureDate());
        rescheduled.setStartTime(request.startTime());
        rescheduled.setEndTime(request.endTime());
        rescheduled.setStatus(LectureStatus.SCHEDULED);

        return lectureMapper.toLectureResponse(lectureRepository.save(rescheduled));
    }

    @Override
    @Transactional
    public int publishStartReminders() {
        LocalDate today = DateTimeUtil.today();
        LocalTime now = DateTimeUtil.now().toLocalTime();

        // Bounded to today's rows: series materialisation keeps weeks of
        // SCHEDULED lectures ahead, and a reminder can only ever concern today.
        List<Lecture> startingSoon = lectureRepository
                .findByStatusAndLectureDate(LectureStatus.SCHEDULED, today).stream()
                .filter(lecture -> !lecture.isStartReminderSent()
                        && lecture.getStartTime().isAfter(now)
                        && !lecture.getStartTime().isAfter(now.plusMinutes(REMINDER_WINDOW_MINUTES)))
                .toList();

        for (Lecture lecture : startingSoon) {
            eventPublisher.publishEvent(new LectureStartingSoonEvent(
                    lecture.getId(),
                    lecture.getTeacher().getId(),
                    lecture.getTeacher().getEmail(),
                    lecture.getSubject(),
                    lecture.getClassName(),
                    lecture.getStartTime()));
            lecture.setStartReminderSent(true);
        }
        lectureRepository.saveAll(startingSoon);
        return startingSoon.size();
    }

    @Override
    @Transactional
    public int completeOverdueLectures() {
        LocalDate today = DateTimeUtil.today();
        LocalTime now = DateTimeUtil.now().toLocalTime();

        // Live lectures past their effective end (which accounts for a late
        // actual start and any extensions) finish automatically.
        List<Lecture> overdue = lectureRepository.findByStatus(LectureStatus.LIVE).stream()
                .filter(lecture -> lecture.getLectureDate().isBefore(today)
                        || (lecture.getLectureDate().isEqual(today)
                            && !lecture.getEffectiveEndTime().isAfter(now)))
                .toList();
        overdue.forEach(lecture -> lecture.setStatus(LectureStatus.COMPLETED));
        lectureRepository.saveAll(overdue);

        // Never-started lectures whose scheduled end has passed are missed —
        // auto-cancelled, with a notification pointing at Reschedule. Bounded
        // to today and earlier: future SCHEDULED rows (weeks of them, once
        // series materialise ahead) can never be overdue.
        List<Lecture> missed = lectureRepository
                .findByStatusAndLectureDateLessThanEqual(LectureStatus.SCHEDULED, today).stream()
                .filter(lecture -> lecture.getLectureDate().isBefore(today)
                        || !lecture.getEndTime().isAfter(now))
                .toList();
        missed.forEach(lecture -> {
            lecture.setStatus(LectureStatus.MISSED);
            eventPublisher.publishEvent(new LectureMissedEvent(
                    lecture.getId(),
                    lecture.getTeacher().getId(),
                    lecture.getTeacher().getEmail(),
                    lecture.getSubject(),
                    lecture.getClassName()));
        });
        lectureRepository.saveAll(missed);

        return overdue.size() + missed.size();
    }

    @Override
    @Transactional
    public int publishEndingReminders() {
        LocalDate today = DateTimeUtil.today();
        LocalTime now = DateTimeUtil.now().toLocalTime();

        List<Lecture> endingSoon = lectureRepository.findByStatus(LectureStatus.LIVE).stream()
                .filter(lecture -> !lecture.isReminderSent()
                        && lecture.getLectureDate().isEqual(today)
                        && lecture.getEffectiveEndTime().isAfter(now)
                        && !lecture.getEffectiveEndTime().isAfter(now.plusMinutes(REMINDER_WINDOW_MINUTES)))
                .toList();

        for (Lecture lecture : endingSoon) {
            eventPublisher.publishEvent(new LectureEndingSoonEvent(
                    lecture.getId(),
                    lecture.getTeacher().getId(),
                    lecture.getTeacher().getEmail(),
                    lecture.getTeacher().getFullName(),
                    lecture.getSubject(),
                    lecture.getClassName(),
                    lecture.getEffectiveEndTime()));
            lecture.setReminderSent(true);
        }
        lectureRepository.saveAll(endingSoon);
        return endingSoon.size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureResponse> getMyCalendar(String teacherEmail, LocalDate from, LocalDate to) {
        User teacher = findUserByEmail(teacherEmail);
        validateCalendarRange(from, to);
        return lectureRepository
                .findByTeacherAndLectureDateBetweenOrderByLectureDateAscStartTimeAsc(teacher, from, to)
                .stream()
                .map(lectureMapper::toLectureResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureResponse> getCalendar(LocalDate from, LocalDate to) {
        validateCalendarRange(from, to);
        return lectureRepository
                .findByLectureDateBetweenOrderByLectureDateAscStartTimeAsc(from, to)
                .stream()
                .map(lectureMapper::toLectureResponse)
                .toList();
    }

    @Override
    @Transactional
    public int publishDayBeforeReminders() {
        LocalDate tomorrow = DateTimeUtil.today().plusDays(1);

        // The one-shot flag makes this idempotent per lecture: a later tick
        // of the evening window only picks up classes scheduled after the
        // first digest went out.
        List<Lecture> due = lectureRepository
                .findByLectureDateAndStatusAndDayBeforeReminderSentFalse(
                        tomorrow, LectureStatus.SCHEDULED);
        if (due.isEmpty()) {
            return 0;
        }

        // One digest per teacher, classes in start order. The repository
        // returns rows unordered, so group first and sort each group.
        Map<Long, List<Lecture>> byTeacher = new LinkedHashMap<>();
        for (Lecture lecture : due) {
            byTeacher.computeIfAbsent(lecture.getTeacher().getId(),
                    id -> new ArrayList<>()).add(lecture);
        }

        for (List<Lecture> lectures : byTeacher.values()) {
            lectures.sort(java.util.Comparator.comparing(Lecture::getStartTime));
            List<String> entries = lectures.stream()
                    .map(LectureServiceImpl::formatDigestEntry)
                    .toList();
            User teacher = lectures.get(0).getTeacher();
            eventPublisher.publishEvent(new LecturesTomorrowEvent(
                    teacher.getId(),
                    teacher.getEmail(),
                    teacher.getFullName(),
                    tomorrow,
                    entries));
            lectures.forEach(lecture -> lecture.setDayBeforeReminderSent(true));
        }
        lectureRepository.saveAll(due);
        return byTeacher.size();
    }

    /** One digest line: {@code "10:00–11:00  Maths · Grade 10 (B)"}. */
    private static String formatDigestEntry(Lecture lecture) {
        StringBuilder entry = new StringBuilder()
                .append(lecture.getStartTime()).append('–').append(lecture.getEndTime())
                .append("  ").append(lecture.getSubject())
                .append(" · ").append(lecture.getClassName());
        if (lecture.getBatch() != null) {
            entry.append(" (").append(lecture.getBatch()).append(')');
        }
        return entry.toString();
    }

    private void validateCalendarRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new BadRequestException("A valid from/to date range is required.");
        }
        if (from.plusDays(MAX_CALENDAR_RANGE_DAYS).isBefore(to)) {
            throw new BadRequestException("Calendar range cannot exceed "
                    + MAX_CALENDAR_RANGE_DAYS + " days.");
        }
    }

    /** Treats blank or empty batch input as "no batch" (stored as null). */
    private String normalizeBatch(String batch) {
        if (batch == null || batch.isBlank()) {
            return null;
        }
        return batch.trim();
    }

    /** Loads a lecture, treating other teachers' lectures as nonexistent. */
    private Lecture findOwnedLecture(String teacherEmail, Long lectureId) {
        User teacher = findUserByEmail(teacherEmail);
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture", "id", lectureId));
        if (!lecture.getTeacher().getId().equals(teacher.getId())) {
            throw new ResourceNotFoundException("Lecture", "id", lectureId);
        }
        return lecture;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
