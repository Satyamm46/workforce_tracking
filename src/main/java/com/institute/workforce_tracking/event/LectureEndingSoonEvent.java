package com.institute.workforce_tracking.event;

import java.time.LocalTime;

/**
 * Domain event published when a live lecture is approaching its effective end.
 *
 * <p>Published by the lecture status sweep. Carries an immutable snapshot of
 * everything a notifier needs to compose a reminder — no listener exists yet;
 * the notification milestone subscribes to this event without any change to
 * the publisher.</p>
 *
 * @param lectureId        the lecture's id
 * @param teacherId        the teacher's user id
 * @param teacherEmail     the teacher's email
 * @param teacherFullName  the teacher's display name
 * @param subject          the lecture's subject
 * @param className        the class being taught
 * @param effectiveEndTime when the lecture will end (including extensions)
 */
public record LectureEndingSoonEvent(
        Long lectureId,
        Long teacherId,
        String teacherEmail,
        String teacherFullName,
        String subject,
        String className,
        LocalTime effectiveEndTime
) {
}
