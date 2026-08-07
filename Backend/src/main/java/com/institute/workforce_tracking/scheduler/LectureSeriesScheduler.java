package com.institute.workforce_tracking.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.constants.AppConstants;
import com.institute.workforce_tracking.service.LectureSeriesService;

/**
 * Nightly trigger that keeps every active lecture series materialised through
 * its rolling horizon. As the window slides forward one day per night, each
 * series gains at most a day or two of new occurrences — the sweep is
 * incremental, driven by each series' watermark.
 *
 * <p>Thin, time-triggered adapter with no business logic, matching the other
 * schedulers. A wall-clock job, so the {@code zone} attribute is mandatory —
 * without it the cron would follow the server's default zone.</p>
 */
@Component
public class LectureSeriesScheduler {

    private static final Logger log = LoggerFactory.getLogger(LectureSeriesScheduler.class);

    private final LectureSeriesService lectureSeriesService;

    public LectureSeriesScheduler(LectureSeriesService lectureSeriesService) {
        this.lectureSeriesService = lectureSeriesService;
    }

    /**
     * Tops up all active series shortly after midnight (00:20), off the hour
     * to stay clear of other date-rollover work.
     */
    @Scheduled(cron = "0 20 0 * * *", zone = AppConstants.DEFAULT_TIME_ZONE)
    public void materialiseUpcomingOccurrences() {
        try {
            int created = lectureSeriesService.materialiseUpcoming();
            if (created > 0) {
                log.info("Series materialisation: {} lecture(s) created", created);
            }
        } catch (Exception ex) {
            log.error("Series materialisation sweep failed", ex);
        }
    }
}
