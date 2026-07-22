package com.institute.workforce_tracking.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Outbound DTO for a lecture summary.
 *
 * @param id              summary primary key
 * @param lectureId       the lecture's id
 * @param teacherName     the teacher's full name
 * @param subject         the subject taught
 * @param className       the class
 * @param lectureDate     the lecture day
 * @param startTime       scheduled start
 * @param endTime         scheduled end
 * @param summaryText     what was covered
 * @param submittedAt     when the summary was submitted
 * @param lectureEndTime  the lecture's actual end time (deadline = this + 24h)
 * @param submittedLate   whether submitted after the 24h window
 */
public record LectureSummaryResponse(
        Long id,
        Long lectureId,
        String teacherName,
        String subject,
        String className,
        LocalDate lectureDate,
        LocalTime startTime,
        LocalTime endTime,
        String summaryText,
        LocalDateTime submittedAt,
        LocalDateTime lectureEndTime,
        boolean submittedLate
) {
}
