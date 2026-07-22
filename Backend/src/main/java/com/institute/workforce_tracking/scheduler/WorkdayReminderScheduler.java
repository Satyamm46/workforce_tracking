package com.institute.workforce_tracking.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.service.AttendanceService;

/**
 * Time-driven workday reminders and overtime enforcement.
 *
 * <p>Runs once a minute and delegates to {@link AttendanceService}:</p>
 * <ol>
 *   <li><b>Start reminders</b> — a heads-up to each user shortly before the
 *       work-start time they declared, if they have not checked in yet.</li>
 *   <li><b>Overtime</b> — opens a window when a WORKING employee passes their
 *       declared logout time, warns before it closes, and auto-checks-out
 *       anyone whose window closes without an extension.</li>
 * </ol>
 *
 * <p>Thin adapter like {@link LectureStatusScheduler} and
 * {@link AttendanceAutoScheduler}: the rules and transactions live in the
 * service, and each sweep is individually fail-safe.</p>
 */
@Component
public class WorkdayReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(WorkdayReminderScheduler.class);

    private final AttendanceService attendanceService;

    public WorkdayReminderScheduler(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void sweep() {
        int reminders = runSweep("start-reminder", attendanceService::publishStartReminders);
        int overtime = runSweep("overtime", attendanceService::processOvertimeWindows);

        if (reminders > 0 || overtime > 0) {
            log.info("Workday sweep: {} start reminder(s), {} overtime action(s)",
                    reminders, overtime);
        }
    }

    /** Runs one sweep, logging and swallowing any failure. */
    private int runSweep(String name, SweepAction action) {
        try {
            return action.run();
        } catch (Exception ex) {
            log.error("Workday {} sweep failed", name, ex);
            return 0;
        }
    }

    @FunctionalInterface
    private interface SweepAction {
        int run();
    }
}
