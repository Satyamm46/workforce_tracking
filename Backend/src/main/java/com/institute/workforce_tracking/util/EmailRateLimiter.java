package com.institute.workforce_tracking.util;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.exception.TooManyRequestsException;

/**
 * In-memory, per-email rate limiter for abuse-prone public endpoints.
 *
 * <p>Tracks attempt counts in a sliding window. Entries expire automatically
 * on the next check once the window has passed — no background thread needed.
 * Keyed on email rather than IP because Cloudflare Tunnel and Tailscale Funnel
 * both forward requests from a loopback address, making IP useless here.</p>
 */
@Component
public class EmailRateLimiter {

    private record Window(int count, Instant windowStart) {}

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    private final int maxAttempts;
    private final long windowSeconds;

    public EmailRateLimiter(
            @Value("${app.rate-limit.max-attempts:10}") int maxAttempts,
            @Value("${app.rate-limit.window-seconds:60}") long windowSeconds) {
        this.maxAttempts = maxAttempts;
        this.windowSeconds = windowSeconds;
    }

    /**
     * Records one attempt for {@code email} and throws if the limit is exceeded.
     *
     * @param email the address being rate-limited
     * @throws TooManyRequestsException when the caller has exceeded the limit
     */
    public void check(String email) {
        Instant now = Instant.now();
        Window updated = windows.compute(email, (k, existing) -> {
            if (existing == null || now.isAfter(existing.windowStart().plusSeconds(windowSeconds))) {
                return new Window(1, now);
            }
            return new Window(existing.count() + 1, existing.windowStart());
        });
        if (updated.count() > maxAttempts) {
            throw new TooManyRequestsException(
                    "Too many attempts for this address. Please wait a moment and try again.");
        }
    }
}
