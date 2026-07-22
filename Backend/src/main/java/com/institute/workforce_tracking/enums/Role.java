package com.institute.workforce_tracking.enums;

/**
 * The set of roles a user can hold in the system.
 *
 * <p>Each user is assigned exactly one role, which determines what they are
 * authorized to do. Roles are the foundation of the application's Role-Based
 * Access Control (RBAC): authorization rules are expressed in terms of these
 * values, and the role is embedded as a claim in the user's JWT.</p>
 *
 * <p>This enum is a pure domain concept and is intentionally decoupled from
 * Spring Security. The mapping to a Spring {@code GrantedAuthority} (including
 * the conventional {@code "ROLE_"} prefix) is performed in the security layer,
 * not here — so the domain does not depend on the security framework.</p>
 */
public enum Role {

    /**
     * Complete system access. Manages employees and teachers, views all
     * reports and the live dashboard. The highest privilege level.
     */
    SUPER_ADMIN,

    /**
     * Administrative / HR operations: manages attendance, approves or rejects
     * leave, views workforce activity, and manages teacher schedules.
     */
    ADMIN,

    /**
     * An employee who also teaches. Has all employee capabilities plus lecture
     * scheduling, lecture tracking, and lecture extensions.
     */
    TEACHER,

    /**
     * A standard employee: logs in/out, manages breaks, views attendance
     * history, and applies for leave. The baseline role.
     */
    EMPLOYEE
}