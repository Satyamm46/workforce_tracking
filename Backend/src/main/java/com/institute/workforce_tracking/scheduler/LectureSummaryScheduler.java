package com.institute.workforce_tracking.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.service.LectureSummaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled sweep marking completed lectures as SUMMARY_MISSED when the
 * teacher did not submit a summary within 24 hours of the lecture ending.
 * Runs every 15 minutes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LectureSummaryScheduler {

    private final LectureSummaryService lectureSummaryService;

    @Scheduled(fixedDelay = 900_000) // 15 minutes
    public void cancelLecturesWithMissingSummaries() {
        try {
            int marked = lectureSummaryService.cancelLecturesWithMissingSummaries();
            if (marked > 0) {
                log.info("Marked {} lecture(s) as summary-missed", marked);
            }
        } catch (Exception ex) {
            log.error("Lecture summary sweep failed", ex);
        }
    }
}
