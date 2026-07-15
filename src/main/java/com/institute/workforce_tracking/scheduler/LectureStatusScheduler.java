package com.institute.workforce_tracking.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.service.LectureService;

/**
 * Time-driven trigger for lecture lifecycle transitions.
 *
 * <p>Runs once a minute and delegates to {@link LectureService} for the three
 * sweep operations: due lectures go live, overdue lectures complete, and
 * ending-soon reminders are published. This class is a thin, time-triggered
 * adapter — the scheduling counterpart of a controller — and contains no
 * business logic.</p>
 *
 * <p>Each sweep is individually guarded: a failure in one is logged and must
 * never prevent the others from running, nor kill subsequent ticks.</p>
 */
@Component
public class LectureStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(LectureStatusScheduler.class);

    private final LectureService lectureService;

    public LectureStatusScheduler(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    /**
     * The lecture status sweep, at second 10 of every minute.
     */
    @Scheduled(cron = "10 * * * * *")
    public void sweepLectureStatuses() {
        int wentLive = runSweep("go-live", lectureService::goLiveDueLectures);
        int completed = runSweep("complete", lectureService::completeOverdueLectures);
        int reminded = runSweep("reminder", lectureService::publishEndingReminders);

        if (wentLive > 0 || completed > 0 || reminded > 0) {
            log.info("Lecture sweep: {} went live, {} completed, {} reminders published",
                    wentLive, completed, reminded);
        }
    }

    /**
     * Runs one sweep operation, converting any failure into a log entry so
     * the remaining sweeps and future ticks are unaffected.
     */
    private int runSweep(String name, SweepOperation operation) {
        try {
            return operation.run();
        } catch (Exception ex) {
            log.error("Lecture {} sweep failed", name, ex);
            return 0;
        }
    }

    /** A sweep operation returning how many lectures it affected. */
    @FunctionalInterface
    private interface SweepOperation {
        int run();
    }
}
