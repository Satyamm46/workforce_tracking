package com.institute.workforce_tracking.service.impl;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.request.ScheduleLectureRequest;
import com.institute.workforce_tracking.dto.response.LectureResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.entity.Lecture;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.enums.LectureStatus;
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
 */
@Service
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;
    private final LectureMapper lectureMapper;

    public LectureServiceImpl(LectureRepository lectureRepository,
                              UserRepository userRepository,
                              LectureMapper lectureMapper) {
        this.lectureRepository = lectureRepository;
        this.userRepository = userRepository;
        this.lectureMapper = lectureMapper;
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
        User teacher = findUserByEmail(teacherEmail);
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture", "id", lectureId));

        // Ownership guard: other teachers' lectures are treated as nonexistent.
        if (!lecture.getTeacher().getId().equals(teacher.getId())) {
            throw new ResourceNotFoundException("Lecture", "id", lectureId);
        }
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
