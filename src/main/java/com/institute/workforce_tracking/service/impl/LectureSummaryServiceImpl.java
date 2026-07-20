package com.institute.workforce_tracking.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.request.SubmitLectureSummaryRequest;
import com.institute.workforce_tracking.dto.response.LectureSummaryResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.entity.DeadlineExtension;
import com.institute.workforce_tracking.entity.Lecture;
import com.institute.workforce_tracking.entity.LectureSummary;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.enums.DeadlineType;
import com.institute.workforce_tracking.enums.LectureStatus;
import com.institute.workforce_tracking.exception.BadRequestException;
import com.institute.workforce_tracking.exception.ResourceNotFoundException;
import com.institute.workforce_tracking.mapper.LectureSummaryMapper;
import com.institute.workforce_tracking.repository.DeadlineExtensionRepository;
import com.institute.workforce_tracking.repository.LectureRepository;
import com.institute.workforce_tracking.repository.LectureSummaryRepository;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.service.LectureSummaryService;
import com.institute.workforce_tracking.util.DateTimeUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LectureSummaryServiceImpl implements LectureSummaryService {

    private final LectureSummaryRepository summaryRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;
    private final LectureSummaryMapper summaryMapper;
    private final DeadlineExtensionRepository deadlineExtensionRepository;

    private static final int BASE_DEADLINE_HOURS = 24;

    @Override
    @Transactional
    public LectureSummaryResponse submitSummary(String teacherEmail, Long lectureId,
                                                SubmitLectureSummaryRequest request) {
        User teacher = findUserByEmail(teacherEmail);
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture", "id", lectureId));

        if (!lecture.getTeacher().getId().equals(teacher.getId())) {
            throw new ResourceNotFoundException("Lecture", "id", lectureId);
        }
        if (lecture.getStatus() != LectureStatus.COMPLETED) {
            throw new BadRequestException(
                    "A summary can only be submitted for a completed lecture.");
        }
        if (summaryRepository.existsByLecture(lecture)) {
            throw new BadRequestException(
                    "A summary has already been submitted for this lecture.");
        }

        // The effective end is the actual end (accounting for late start + extensions).
        LocalTime effectiveEnd = lecture.getEffectiveEndTime();
        LocalDateTime lectureEndTime = lecture.getLectureDate().atTime(effectiveEnd);

        LectureSummary summary = new LectureSummary();
        summary.setLecture(lecture);
        summary.setSummaryText(request.summaryText());
        summary.setSubmittedAt(DateTimeUtil.now());
        summary.setLectureEndTime(lectureEndTime);
        summary.setSubmittedLate(summary.getSubmittedAt()
                .isAfter(effectiveDeadline(teacher, lecture, lectureEndTime)));

        return summaryMapper.toLectureSummaryResponse(summaryRepository.save(summary));
    }

    @Override
    @Transactional(readOnly = true)
    public LectureSummaryResponse getMySummaryForLecture(String teacherEmail, Long lectureId) {
        User teacher = findUserByEmail(teacherEmail);
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture", "id", lectureId));

        if (!lecture.getTeacher().getId().equals(teacher.getId())) {
            throw new ResourceNotFoundException("Lecture", "id", lectureId);
        }

        LectureSummary summary = summaryRepository.findByLecture(lecture)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No summary found for lecture " + lectureId + "."));
        return summaryMapper.toLectureSummaryResponse(summary);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<LectureSummaryResponse> getMySummaries(String teacherEmail,
                                                                 int page, int size) {
        User teacher = findUserByEmail(teacherEmail);
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "lecture.lectureDate"));
        Page<LectureSummary> summaries = summaryRepository.findByTeacher(teacher, pageable);
        return PagedResponse.of(summaries, summaryMapper::toLectureSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<LectureSummaryResponse> getSummariesByDate(LocalDate date,
                                                                     int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "submittedAt"));
        Page<LectureSummary> summaries = summaryRepository.findByLectureDate(date, pageable);
        return PagedResponse.of(summaries, summaryMapper::toLectureSummaryResponse);
    }

    @Override
    @Transactional
    public int cancelLecturesWithMissingSummaries() {
        LocalDateTime now = DateTimeUtil.now();

        // COMPLETED lectures past their effective deadline (base 24h plus any
        // admin-granted extension) without a summary.
        List<Lecture> overdue = lectureRepository.findByStatus(LectureStatus.COMPLETED)
                .stream()
                .filter(lecture -> {
                    LocalDateTime endDatetime = lecture.getLectureDate()
                            .atTime(lecture.getEffectiveEndTime());
                    return now.isAfter(effectiveDeadline(
                                    lecture.getTeacher(), lecture, endDatetime))
                            && !summaryRepository.existsByLecture(lecture);
                })
                .toList();

        overdue.forEach(lecture -> {
            lecture.setStatus(LectureStatus.SUMMARY_MISSED);
            log.info("Lecture {} ({} for {}) summary overdue — marked summary-missed",
                    lecture.getId(), lecture.getSubject(), lecture.getClassName());
        });
        lectureRepository.saveAll(overdue);
        return overdue.size();
    }

    /** Lecture end + 24h, plus any admin-granted extension for that day. */
    private LocalDateTime effectiveDeadline(User teacher, Lecture lecture,
                                            LocalDateTime lectureEndTime) {
        int extraHours = deadlineExtensionRepository
                .findByUserAndTypeAndTargetDate(teacher, DeadlineType.LECTURE_SUMMARY,
                        lecture.getLectureDate())
                .map(DeadlineExtension::getExtraHours)
                .orElse(0);
        return lectureEndTime.plusHours(BASE_DEADLINE_HOURS + extraHours);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
