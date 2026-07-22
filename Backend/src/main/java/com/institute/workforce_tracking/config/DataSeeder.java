package com.institute.workforce_tracking.config;

import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.enums.Role;
import com.institute.workforce_tracking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the initial Super Admin account on application startup.
 *
 * <p>Because users are created by administrators (not via public
 * registration), the first Super Admin must be bootstrapped programmatically —
 * otherwise the system would have no account to log in with. This runner is
 * idempotent: it only creates the account if no Super Admin already exists, so
 * it is safe to run on every startup.</p>
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String adminEmail;
    private final String adminPassword;
    private final String adminFullName;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.seed.super-admin.email}") String adminEmail,
                      @Value("${app.seed.super-admin.password}") String adminPassword,
                      @Value("${app.seed.super-admin.full-name}") String adminFullName) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminFullName = adminFullName;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(Role.SUPER_ADMIN)) {
            log.info("Super Admin already present — skipping data seed.");
            return;
        }

        User superAdmin = new User();
        superAdmin.setFullName(adminFullName);
        superAdmin.setEmail(adminEmail);
        superAdmin.setPassword(passwordEncoder.encode(adminPassword));
        superAdmin.setRole(Role.SUPER_ADMIN);
        superAdmin.setEnabled(true);

        userRepository.save(superAdmin);

        log.info("Seeded initial Super Admin with email '{}'. "
                + "Change this password immediately in any real environment.", adminEmail);
    }
}