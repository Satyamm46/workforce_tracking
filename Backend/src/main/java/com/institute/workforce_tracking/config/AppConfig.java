package com.institute.workforce_tracking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Application-wide bean definitions that are not tied to any single feature.
 *
 * <p>This is the home for shared infrastructure beans the whole application
 * depends on. Keeping them here — rather than scattered across feature
 * classes or declared inside {@code SecurityConfig} — gives a single, obvious
 * place to find cross-cutting beans as the system grows.</p>
 */
@Configuration
public class AppConfig {

    /**
     * The password hashing strategy for the entire application.
     *
     * <p>Exposed as the {@link PasswordEncoder} interface (not the concrete
     * {@link BCryptPasswordEncoder}) so callers depend on the abstraction.
     * The implementation can be swapped later — e.g. to Argon2 — by changing
     * only this method; no caller changes.</p>
     *
     * <p>BCrypt automatically generates and stores a per-password salt inside
     * the resulting hash, so no separate salt management is required.</p>
     *
     * @return the singleton password encoder used to hash and verify passwords
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}