package com.institute.workforce_tracking.event;

import java.time.LocalDate;
import java.util.List;

/**
 * Domain event published the evening before a teaching day: one digest per
 * teacher listing every class they have tomorrow, so the day can be planned
 * in advance — unlike {@link LectureStartingSoonEvent}, which fires five
 * minutes before a single class.
 *
 * @param teacherId       the teacher's user id
 * @param teacherEmail    the teacher's email (WebSocket principal + mail to)
 * @param teacherFullName the teacher's display name, for the email greeting
 * @param date            the day the classes take place (tomorrow)
 * @param entries         one preformatted line per class, in start order,
 *                        e.g. {@code "10:00–11:00  Maths · Grade 10 (B)"}
 */
public record LecturesTomorrowEvent(
        Long teacherId,
        String teacherEmail,
        String teacherFullName,
        LocalDate date,
        List<String> entries
) {
}
