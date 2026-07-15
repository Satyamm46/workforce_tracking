package com.institute.workforce_tracking.dto.response;

/**
 * One teacher's activity summary for a reporting period. Built in Java by the
 * report service from the period's completed lectures.
 *
 * @param teacherId         the teacher's user id
 * @param fullName          the teacher's name
 * @param lecturesCompleted lectures conducted in the period
 * @param teachingMinutes   total minutes taught (durations plus extensions)
 * @param extensionMinutes  total extension minutes used
 */
public record TeacherReportRow(
        Long teacherId,
        String fullName,
        long lecturesCompleted,
        long teachingMinutes,
        long extensionMinutes
) {
}
