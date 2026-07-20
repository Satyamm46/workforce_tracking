package com.institute.workforce_tracking.mapper;

import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.dto.response.WorkReportResponse;
import com.institute.workforce_tracking.entity.WorkReport;

@Component
public class WorkReportMapper {

    public WorkReportResponse toWorkReportResponse(WorkReport report) {
        return new WorkReportResponse(
                report.getId(),
                report.getUser().getId(),
                report.getUser().getFullName(),
                report.getWorkDate(),
                report.getReportText(),
                report.getSubmittedAt(),
                report.getCheckInTime(),
                report.getCheckoutTime(),
                report.getPlannedStartTime(),
                report.getPlannedEndTime(),
                report.getPlannedWork(),
                report.isSubmittedLate());
    }
}
