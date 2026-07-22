package com.institute.workforce_tracking.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.institute.workforce_tracking.service.EmailService;

/**
 * SMTP-backed implementation of {@link EmailService}.
 *
 * <p>Sends asynchronously so a slow mail server never delays the request
 * thread, and swallows (logs) all failures — email here is a notification
 * courtesy, not a business-critical operation. When no SMTP username is
 * configured (typical local dev), sending is skipped with a log line instead
 * of a connection error.</p>
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String from;

    public EmailServiceImpl(JavaMailSender mailSender,
                            @Value("${spring.mail.username:}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    @Async
    public void send(String to, String subject, String body) {
        if (from == null || from.isBlank()) {
            log.info("Mail not configured (spring.mail.username empty) — skipping email to {} [{}]",
                    to, subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.debug("Email sent to {} [{}]", to, subject);
        } catch (Exception ex) {
            log.error("Failed to send email to {} [{}]: {}", to, subject, ex.getMessage());
        }
    }
}
