package com.institute.workforce_tracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application entry point for the Institute Workforce Tracking System.
 *
 * <p>This class bootstraps the entire Spring context. It intentionally
 * contains no business logic — its only job is to start the application
 * and switch on the cross-cutting capabilities the foundation needs.</p>
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class WorkforceTrackingApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkforceTrackingApplication.class, args);
    }
}