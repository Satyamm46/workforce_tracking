package com.institute.workforce_tracking.event;

import java.time.LocalTime;

/**
 * Domain event published ~5 minutes before a lecture's scheduled start, so
 * the teacher gets a heads-up to begin on time.
 *
 * @param lectureId       the lecture about to start
 * @param teacherId       the teacher's user id
 * @param teacherEmail    the teacher's email (WebSocket principal)
 * @param subject         subject being taught
 * @param className       the class
 * @param scheduledStart  when the lecture is scheduled to begin
 */
public record LectureStartingSoonEvent(
        Long lectureId,
        Long teacherId,
        String teacherEmail,
        String subject,
        String className,
        LocalTime scheduledStart
) {
}
