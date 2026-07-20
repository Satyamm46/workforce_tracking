package com.institute.workforce_tracking.service.impl;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.entity.EmailVerification;
import com.institute.workforce_tracking.exception.BadRequestException;
import com.institute.workforce_tracking.repository.EmailVerificationRepository;
import com.institute.workforce_tracking.service.EmailVerificationService;

/**
 * Default implementation of {@link EmailVerificationService}.
 *
 * <p>Unlike the courtesy {@code EmailService}, the OTP email is sent
 * synchronously and its failure is propagated: if we cannot deliver the code,
 * the applicant must be told rather than left waiting for a code that will
 * never arrive.</p>
 */
@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);

    /** Code lifetime — long enough to fetch from an inbox, short enough to limit exposure. */
    private static final int CODE_TTL_MINUTES = 10;

    /** Max verification attempts before a code is burned and a new one is required. */
    private static final int MAX_ATTEMPTS = 5;

    private final EmailVerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String from;
    private final SecureRandom random = new SecureRandom();

    public EmailVerificationServiceImpl(EmailVerificationRepository verificationRepository,
                                        PasswordEncoder passwordEncoder,
                                        JavaMailSender mailSender,
                                        @Value("${spring.mail.username:}") String from) {
        this.verificationRepository = verificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    @Transactional
    public void sendCode(String email) {
        String normalized = normalize(email);
        String code = generateCode();

        // One row per email: reuse the existing row (fresh code overwrites the
        // old one), or create it the first time.
        EmailVerification verification = verificationRepository.findByEmail(normalized)
                .orElseGet(EmailVerification::new);
        verification.setEmail(normalized);
        verification.setCodeHash(passwordEncoder.encode(code));
        verification.setExpiresAt(Instant.now().plus(CODE_TTL_MINUTES, ChronoUnit.MINUTES));
        verification.setAttempts(0);
        verification.setConsumed(false);
        verificationRepository.save(verification);

        sendEmail(normalized, code);
    }

    @Override
    @Transactional
    public void verifyCode(String email, String code) {
        String normalized = normalize(email);
        EmailVerification verification = verificationRepository.findByEmail(normalized)
                .orElseThrow(() -> new BadRequestException(
                        "No verification code was requested for this email. Request a code first."));

        if (verification.isConsumed()) {
            throw new BadRequestException(
                    "This code has already been used. Request a new code.");
        }
        if (Instant.now().isAfter(verification.getExpiresAt())) {
            throw new BadRequestException(
                    "The verification code has expired. Request a new code.");
        }
        if (verification.getAttempts() >= MAX_ATTEMPTS) {
            throw new BadRequestException(
                    "Too many incorrect attempts. Request a new code.");
        }

        if (!passwordEncoder.matches(code == null ? "" : code.trim(), verification.getCodeHash())) {
            verification.setAttempts(verification.getAttempts() + 1);
            verificationRepository.save(verification);
            throw new BadRequestException("Incorrect verification code.");
        }

        // Correct: burn the code so it cannot be replayed for another sign-up.
        verification.setConsumed(true);
        verificationRepository.save(verification);
    }

    /** Six-digit numeric code, zero-padded (000000–999999). */
    private String generateCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private void sendEmail(String to, String code) {
        if (from == null || from.isBlank()) {
            // Local dev without SMTP configured: log the code so registration
            // can still be exercised end to end, and don't fail the request.
            log.warn("Mail not configured — verification code for {} is {}", to, code);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("Your verification code");
            message.setText("Your registration verification code is: " + code
                    + "\n\nIt expires in " + CODE_TTL_MINUTES + " minutes. "
                    + "If you did not request this, you can ignore this email.");
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send verification email to {}: {}", to, ex.getMessage());
            throw new BadRequestException(
                    "Could not send the verification email. Check the address and try again.");
        }
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
