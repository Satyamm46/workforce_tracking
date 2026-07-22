package com.institute.workforce_tracking.event;

/**
 * Domain event published when a lecture is auto-cancelled because the teacher
 * never started it within its scheduled window. The listener tells the
 * teacher and points them at the reschedule action.
 *
 * @param lectureId    the missed lecture
 * @param teacherId    the teacher's user id
 * @param teacherEmail the teacher's email (WebSocket principal)
 * @param subject      subject being taught
 * @param className    the class
 */
public record LectureMissedEvent(
        Long lectureId,
        Long teacherId,
        String teacherEmail,
        String subject,
        String className
) {
}
