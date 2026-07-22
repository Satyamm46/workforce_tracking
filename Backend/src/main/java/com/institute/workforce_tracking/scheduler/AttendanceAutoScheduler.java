package com.institute.workforce_tracking.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.service.AttendanceService;

/**
 * Periodic attendance enforcement.
 *
 * <p>Two sweeps run every minute:</p>
 * <ol>
 *   <li><b>Auto-break</b> — a WORKING user whose browser has been gone longer
 *       than the grace period is placed on a system-started break. Only an
 *       explicit logout clocks a user out; disappearing means "on break".</li>
 *   <li><b>Auto-checkout</b> — a system-started break that exceeds the
 *       configured limit ends the day. Manual breaks are never affected.</li>
 * </ol>
 *
 * <p>Thin adapter like {@link LectureStatusScheduler}: transactions and rules
 * live in {@link AttendanceService}; each sweep is individually fail-safe so
 * one failure never blocks the other.</p>
 */
@Component
public class AttendanceAutoScheduler {

    private static final Logger log = LoggerFactory.getLogger(AttendanceAutoScheduler.class);

    private final AttendanceService attendanceService;
    private final long offlineGraceSeconds;
    private final long maxAutoBreakMinutes;

    public AttendanceAutoScheduler(
            AttendanceService attendanceService,
            @Value("${app.attendance.offline-grace-seconds:90}") long offlineGraceSeconds,
            @Value("${app.attendance.auto-checkout-break-minutes:30}") long maxAutoBreakMinutes) {
        this.attendanceService = attendanceService;
        this.offlineGraceSeconds = offlineGraceSeconds;
        this.maxAutoBreakMinutes = maxAutoBreakMinutes;
    }

    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void enforceAttendance() {
        int breaks = runSweep("auto-break",
                () -> attendanceService.autoBreakAbsentUsers(offlineGraceSeconds));
        int checkouts = runSweep("auto-checkout",
                () -> attendanceService.autoCheckoutOverdueBreaks(maxAutoBreakMinutes));

        if (breaks > 0 || checkouts > 0) {
            log.info("Attendance sweep: {} auto-break(s), {} auto-checkout(s)", breaks, checkouts);
        }
    }

    /** Runs one sweep, logging and swallowing any failure. */
    private int runSweep(String name, SweepAction action) {
        try {
            return action.run();
        } catch (Exception ex) {
            log.error("Attendance {} sweep failed", name, ex);
            return 0;
        }
    }

    @FunctionalInterface
    private interface SweepAction {
        int run();
    }
}
