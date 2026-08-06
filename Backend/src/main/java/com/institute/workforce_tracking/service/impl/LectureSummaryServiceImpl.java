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
        // Once the sweep has run, a lapsed lecture carries this status, and it
        // is worth its own message: the teacher's next step is asking an admin,
        // not waiting for the lecture to complete.
        if (lecture.getStatus() == LectureStatus.SUMMARY_MISSED) {
            throw new BadRequestException(
                    "The summary window for this lecture has closed, so it stays marked "
                            + "summary-missed. Only an admin can reopen it by extending the "
                            + "lecture summary deadline for " + lecture.getLectureDate() + ".");
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

        // The same window the sweep punishes, enforced here so the two cannot
        // disagree. The sweep runs every 15 minutes, so without this check a
        // lecture stays submittable for up to a quarter of an hour past its
        // deadline purely because nothing has got round to marking it yet.
        LocalDateTime deadline = effectiveDeadline(teacher, lecture, lectureEndTime);
        if (DateTimeUtil.now().isAfter(deadline)) {
            throw new BadRequestException(
                    "The " + (hasExtension(teacher, lecture) ? "extended " : "")
                            + "summary window for this lecture closed on "
                            + DateTimeUtil.formatDateTime(deadline)
                            + ". Ask an admin to extend the lecture summary deadline for "
                            + lecture.getLectureDate() + ".");
        }

        LectureSummary summary = new LectureSummary();
        summary.setLecture(lecture);
        summary.setSummaryText(request.summaryText());
        summary.setSubmittedAt(DateTimeUtil.now());
        summary.setLectureEndTime(lectureEndTime);
        // Against the normal window, not the extended one — past the extended
        // deadline nothing is accepted, so that comparison could never be true.
        summary.setSubmittedLate(summary.getSubmittedAt()
                .isAfter(lectureEndTime.plusHours(BASE_DEADLINE_HOURS)));

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
    @Transactional(readOnly = true)
    public PagedResponse<LectureSummaryResponse> getSummariesByDateRange(LocalDate start, LocalDate end,
                                                                     int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "submittedAt"));
        Page<LectureSummary> summaries =
                summaryRepository.findByLectureDateBetween(start, end, pageable);
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
        return lectureEndTime.plusHours(BASE_DEADLINE_HOURS + extraHours(teacher, lecture));
    }

    /** Whether an admin has already bought this teacher time on this lecture's day. */
    private boolean hasExtension(User teacher, Lecture lecture) {
        return extraHours(teacher, lecture) > 0;
    }

    /** Hours an admin added to this teacher's summary deadline for the lecture's day. */
    private int extraHours(User teacher, Lecture lecture) {
        return deadlineExtensionRepository
                .findByUserAndTypeAndTargetDate(teacher, DeadlineType.LECTURE_SUMMARY,
                        lecture.getLectureDate())
                .map(DeadlineExtension::getExtraHours)
                .orElse(0);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
