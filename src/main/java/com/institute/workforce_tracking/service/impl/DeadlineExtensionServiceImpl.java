package com.institute.workforce_tracking.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.request.GrantDeadlineExtensionRequest;
import com.institute.workforce_tracking.dto.response.DeadlineExtensionResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.entity.DeadlineExtension;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.enums.DeadlineType;
import com.institute.workforce_tracking.enums.LectureStatus;
import com.institute.workforce_tracking.enums.NotificationType;
import com.institute.workforce_tracking.exception.ResourceNotFoundException;
import com.institute.workforce_tracking.repository.AttendanceRepository;
import com.institute.workforce_tracking.repository.DeadlineExtensionRepository;
import com.institute.workforce_tracking.repository.LectureRepository;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.repository.WorkPlanRepository;
import com.institute.workforce_tracking.service.DeadlineExtensionService;
import com.institute.workforce_tracking.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeadlineExtensionServiceImpl implements DeadlineExtensionService {

    private static final int DEFAULT_EXTRA_HOURS = 48;

    private final DeadlineExtensionRepository extensionRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final LectureRepository lectureRepository;
    private final WorkPlanRepository workPlanRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public DeadlineExtensionResponse grantExtension(String adminEmail,
                                                    GrantDeadlineExtensionRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.userId()));

        // Granting twice updates the existing extension instead of failing.
        DeadlineExtension extension = extensionRepository
                .findByUserAndTypeAndTargetDate(user, request.type(), request.targetDate())
                .orElseGet(DeadlineExtension::new);
        extension.setUser(user);
        extension.setType(request.type());
        extension.setTargetDate(request.targetDate());
        extension.setExtraHours(request.extraHours() != null
                ? request.extraHours() : DEFAULT_EXTRA_HOURS);
        extension.setGrantedBy(adminEmail);
        extension.setReason(normalizeReason(request.reason()));
        extension = extensionRepository.save(extension);

        reversePenalty(user, request.type(), request.targetDate());
        notifyUser(user, extension);

        return toResponse(extension);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DeadlineExtensionResponse> getExtensions(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<DeadlineExtension> extensions =
                extensionRepository.findAllByOrderByTargetDateDesc(pageable);
        return PagedResponse.of(extensions, this::toResponse);
    }

    @Override
    @Transactional
    public void revokeExtension(Long extensionId) {
        DeadlineExtension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Deadline extension", "id", extensionId));
        extensionRepository.delete(extension);
    }

    /**
     * Undoes a penalty the sweeps may already have applied for this user and
     * day, so an extension granted after the fact still helps.
     */
    private void reversePenalty(User user, DeadlineType type, java.time.LocalDate targetDate) {
        switch (type) {
            case WORK_REPORT -> attendanceRepository.findByUserAndWorkDate(user, targetDate)
                    .filter(com.institute.workforce_tracking.entity.Attendance::isAbsentNoReport)
                    .ifPresent(attendance -> {
                        attendance.setAbsentNoReport(false);
                        attendanceRepository.save(attendance);
                        log.info("Reversed no-report absence for {} on {}",
                                user.getEmail(), targetDate);
                    });
            case LECTURE_SUMMARY -> {
                var reopened = lectureRepository
                        .findByTeacherAndLectureDateAndStatus(
                                user, targetDate, LectureStatus.SUMMARY_MISSED);
                reopened.forEach(lecture -> lecture.setStatus(LectureStatus.COMPLETED));
                lectureRepository.saveAll(reopened);
                if (!reopened.isEmpty()) {
                    log.info("Restored {} summary-missed lecture(s) for {} on {}",
                            reopened.size(), user.getEmail(), targetDate);
                }
            }
            case WORK_PLAN -> workPlanRepository.findByUserAndPlanDate(user, targetDate)
                    .filter(com.institute.workforce_tracking.entity.WorkPlan::isSubmittedLate)
                    .ifPresent(plan -> {
                        plan.setSubmittedLate(false);
                        workPlanRepository.save(plan);
                        log.info("Cleared late flag on work plan for {} on {}",
                                user.getEmail(), targetDate);
                    });
        }
    }

    /** Best-effort: an extension must not fail because a notification did. */
    private void notifyUser(User user, DeadlineExtension extension) {
        try {
            notificationService.notifyUser(
                    user.getId(), user.getEmail(), NotificationType.DEADLINE_EXTENDED,
                    "Your " + describe(extension.getType()) + " deadline for "
                            + extension.getTargetDate() + " was extended by "
                            + extension.getExtraHours() + " hours."
                            + (extension.getReason() != null
                                    ? " Reason: " + extension.getReason() : ""));
        } catch (Exception ex) {
            log.error("Failed to notify {} about deadline extension", user.getEmail(), ex);
        }
    }

    private String describe(DeadlineType type) {
        return switch (type) {
            case WORK_PLAN -> "work schedule";
            case WORK_REPORT -> "work report";
            case LECTURE_SUMMARY -> "lecture summary";
        };
    }

    private String normalizeReason(String reason) {
        return (reason == null || reason.isBlank()) ? null : reason.trim();
    }

    private DeadlineExtensionResponse toResponse(DeadlineExtension extension) {
        return new DeadlineExtensionResponse(
                extension.getId(),
                extension.getUser().getId(),
                extension.getUser().getFullName(),
                extension.getType(),
                extension.getTargetDate(),
                extension.getExtraHours(),
                extension.getGrantedBy(),
                extension.getReason());
    }
}
