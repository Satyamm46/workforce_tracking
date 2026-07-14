package com.institute.workforce_tracking.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import com.institute.workforce_tracking.enums.LectureStatus;

/**
 * Outbound representation of a lecture.
 *
 * @param id              the lecture id
 * @param teacherId       the teacher's user id
 * @param teacherFullName the teacher's name (for admin views)
 * @param subject         subject being taught
 * @param className       the class
 * @param batch           optional batch; null when not applicable
 * @param lectureDate     the day
 * @param startTime       scheduled start
 * @param endTime         scheduled end (before extensions)
 * @param extendedMinutes minutes added by extensions
 * @param effectiveEndTime endTime plus extensions — when the lecture actually ends
 * @param status          lifecycle state
 */
public record LectureResponse(
        Long id,
        Long teacherId,
        String teacherFullName,
        String subject,
        String className,
        String batch,
        LocalDate lectureDate,
        LocalTime startTime,
        LocalTime endTime,
        int extendedMinutes,
        LocalTime effectiveEndTime,
        LectureStatus status
) {
}
