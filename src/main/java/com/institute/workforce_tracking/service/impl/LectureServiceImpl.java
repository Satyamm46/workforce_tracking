package com.institute.workforce_tracking.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

        List<Lecture> startingSoon = lectureRepository.findByStatus(LectureStatus.SCHEDULED).stream()
                .filter(lecture -> !lecture.isStartReminderSent()
                        && lecture.getLectureDate().isEqual(today)
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
        // auto-cancelled, with a notification pointing at Reschedule.
        List<Lecture> missed = lectureRepository.findByStatus(LectureStatus.SCHEDULED).stream()
                .filter(lecture -> lecture.getLectureDate().isBefore(today)
                        || (lecture.getLectureDate().isEqual(today)
                            && !lecture.getEndTime().isAfter(now)))
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
