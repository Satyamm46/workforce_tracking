package com.institute.workforce_tracking.service.impl;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.entity.PasswordResetCode;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.exception.BadRequestException;
import com.institute.workforce_tracking.repository.PasswordResetCodeRepository;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.service.PasswordResetService;

/**
 * Default implementation of {@link PasswordResetService}.
 *
 * <p>Like the registration OTP, the email is sent synchronously and delivery
 * failure is propagated: a user waiting on a code that will never arrive has no
 * way to recover, so they must be told.</p>
 */
@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);

    /** Code lifetime — long enough to fetch from an inbox, short enough to limit exposure. */
    private static final int CODE_TTL_MINUTES = 10;

    /** Max verification attempts before a code is burned and a new one is required. */
    private static final int MAX_ATTEMPTS = 5;

    private final PasswordResetCodeRepository resetCodeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String from;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetServiceImpl(PasswordResetCodeRepository resetCodeRepository,
                                    UserRepository userRepository,
                                    PasswordEncoder passwordEncoder,
                                    JavaMailSender mailSender,
                                    @Value("${spring.mail.username:}") String from) {
        this.resetCodeRepository = resetCodeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    @Transactional
    public void sendCode(String email) {
        String address = trim(email);

        // No account, or a disabled one that could not sign in anyway: do
        // nothing at all. The controller still reports success, so this
        // endpoint reveals nothing about which addresses are registered.
        Optional<User> account = userRepository.findByEmail(address)
                .filter(User::isEnabled);
        if (account.isEmpty()) {
            log.info("Password reset requested for an unknown or disabled account — ignoring.");
            return;
        }

        String code = generateCode();

        // One row per email: reuse the existing row (a fresh code overwrites
        // the old one), or create it the first time.
        String key = key(address);
        PasswordResetCode resetCode = resetCodeRepository.findByEmail(key)
                .orElseGet(PasswordResetCode::new);
        resetCode.setEmail(key);
        resetCode.setCodeHash(passwordEncoder.encode(code));
        resetCode.setExpiresAt(Instant.now().plus(CODE_TTL_MINUTES, ChronoUnit.MINUTES));
        resetCode.setAttempts(0);
        resetCode.setConsumed(false);
        resetCodeRepository.save(resetCode);

        sendEmail(address, account.get().getFullName(), code);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        String address = trim(email);
        PasswordResetCode resetCode = resetCodeRepository.findByEmail(key(address))
                .orElseThrow(() -> new BadRequestException(
                        "No reset code was requested for this email. Request a code first."));

        if (resetCode.isConsumed()) {
            throw new BadRequestException(
                    "This code has already been used. Request a new code.");
        }
        if (Instant.now().isAfter(resetCode.getExpiresAt())) {
            throw new BadRequestException(
                    "The reset code has expired. Request a new code.");
        }
        if (resetCode.getAttempts() >= MAX_ATTEMPTS) {
            throw new BadRequestException(
                    "Too many incorrect attempts. Request a new code.");
        }

        if (!passwordEncoder.matches(code == null ? "" : code.trim(), resetCode.getCodeHash())) {
            resetCode.setAttempts(resetCode.getAttempts() + 1);
            resetCodeRepository.save(resetCode);
            throw new BadRequestException("Incorrect reset code.");
        }

        // The account is re-read here rather than trusted from send time: it may
        // have been disabled or removed while the code was in the user's inbox.
        User user = userRepository.findByEmail(address)
                .filter(User::isEnabled)
                .orElseThrow(() -> new BadRequestException(
                        "This account is no longer active. Contact your administrator."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Burn the code so the same email cannot be replayed to change the
        // password again later.
        resetCode.setConsumed(true);
        resetCodeRepository.save(resetCode);

        log.info("Password reset completed for user id {}", user.getId());
    }

    /** Six-digit numeric code, zero-padded (000000–999999). */
    private String generateCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private void sendEmail(String to, String fullName, String code) {
        if (from == null || from.isBlank()) {
            // Local dev without SMTP configured: log the code so the reset flow
            // can still be exercised end to end, and don't fail the request.
            log.warn("Mail not configured — password reset code for {} is {}", to, code);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("Your password reset code");
            message.setText("Hello " + fullName + ",\n\n"
                    + "Your password reset code is: " + code
                    + "\n\nIt expires in " + CODE_TTL_MINUTES + " minutes. "
                    + "If you did not request a password reset, you can ignore this "
                    + "email — your password has not been changed.");
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send password reset email to {}: {}", to, ex.getMessage());
            throw new BadRequestException(
                    "Could not send the reset email. Please try again in a moment.");
        }
    }

    /**
     * The address as the account lookup sees it. Only whitespace is stripped —
     * {@code users.email} is matched exactly everywhere else in the app
     * (sign-in included), so lower-casing here would fail to find an account
     * stored with capitals.
     */
    private String trim(String email) {
        return email == null ? "" : email.trim();
    }

    /**
     * The key for this table's one-row-per-email constraint. Lower-cased so a
     * user who capitalises differently between requesting and entering the code
     * still lands on the same row.
     */
    private String key(String address) {
        return address.toLowerCase();
    }
}
