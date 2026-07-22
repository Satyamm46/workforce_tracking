package com.institute.workforce_tracking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing for the whole application.
 *
 * <p>Auditing is what makes the {@code @CreatedDate} and {@code @LastModifiedDate}
 * fields in {@link com.institute.workforce_tracking.entity.BaseEntity} populate
 * automatically. Without this, those columns would remain null and violate
 * their {@code nullable = false} constraint on insert.</p>
 *
 * <p>Kept in its own configuration class (rather than on the main application
 * class) so it can be easily excluded in slice tests that don't need auditing.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}