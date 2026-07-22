package com.institute.workforce_tracking.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.dto.response.DashboardStatsResponse;
import com.institute.workforce_tracking.service.DashboardService;

/**
 * Periodically broadcasts fresh dashboard statistics to subscribed admins.
 *
 * <p>Broadcasts only when the snapshot differs from the previously sent one —
 * record value-equality makes the comparison trivial. A quiet institute
 * produces zero traffic; a busy one updates within seconds.</p>
 */
@Component
public class DashboardBroadcastScheduler {

    private static final Logger log = LoggerFactory.getLogger(DashboardBroadcastScheduler.class);

    /** The broadcast destination admins subscribe to. */
    public static final String DASHBOARD_TOPIC = "/topic/dashboard";

    private final DashboardService dashboardService;
    private final SimpMessagingTemplate messagingTemplate;

    /** The last snapshot broadcast; null until the first send. */
    private DashboardStatsResponse lastBroadcast;

    public DashboardBroadcastScheduler(DashboardService dashboardService,
                                       SimpMessagingTemplate messagingTemplate) {
        this.dashboardService = dashboardService;
        this.messagingTemplate = messagingTemplate;
    }

    /** Computes stats every 15 seconds and broadcasts them if changed. */
    @Scheduled(fixedDelay = 15000)
    public void broadcastStats() {
        try {
            DashboardStatsResponse stats = dashboardService.getStats();
            if (stats.equals(lastBroadcast)) {
                return;
            }
            messagingTemplate.convertAndSend(DASHBOARD_TOPIC, stats);
            lastBroadcast = stats;
        } catch (Exception ex) {
            log.error("Dashboard broadcast failed", ex);
        }
    }
}
