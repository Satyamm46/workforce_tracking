package com.institute.workforce_tracking.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.service.WorkReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled sweep marking users absent who checked out ≥24h ago without
 * submitting a work report (EMPLOYEE/ADMIN/SUPER_ADMIN only).
 * Runs every 15 minutes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkReportScheduler {

    private final WorkReportService workReportService;

    @Scheduled(fixedDelay = 900_000) // 15 minutes
    public void markAbsentForMissingReports() {
        try {
            int marked = workReportService.markAbsentForMissingReports();
            if (marked > 0) {
                log.info("Marked {} user(s) absent for missing work reports", marked);
            }
        } catch (Exception ex) {
            log.error("Work report absence sweep failed", ex);
        }
    }
}
