package com.institute.workforce_tracking.service;

/**
 * Outbound email delivery. Implementations must be fail-safe from the
 * caller's perspective: email is a courtesy channel, so a send failure is
 * logged, never propagated.
 */
public interface EmailService {

    /**
     * Sends a plain-text email. Best-effort — never throws.
     *
     * @param to      the recipient address
     * @param subject the subject line
     * @param body    the plain-text body
     */
    void send(String to, String subject, String body);
}
