package com.institute.workforce_tracking.service;

import java.util.List;

import com.institute.workforce_tracking.dto.response.AttendanceReportRow;
import com.institute.workforce_tracking.dto.response.TeacherReportRow;

/**
 * Monthly reporting over attendance and teaching activity.
 */
public interface ReportService {

    /** Per-employee attendance summary for one calendar month. */
    List<AttendanceReportRow> getMonthlyAttendanceReport(int year, int month);

    /** Per-teacher activity summary for one calendar month. */
    List<TeacherReportRow> getMonthlyTeachingReport(int year, int month);
}
