package com.institute.workforce_tracking.event;

/**
 * Domain event published when an employee is checked out automatically because
 * their overtime window closed without an extension. Delivered in-app, by web
 * push, and by email.
 *
 * @param userId the employee's id
 * @param email  the employee's email
 */
public record OvertimeCheckedOutEvent(
        Long userId,
        String email
) {
}
