package com.institute.workforce_tracking.dto.response;

/**
 * A user's leave balance for one year. Computed from approved requests —
 * never stored — so it cannot drift from the underlying data.
 *
 * @param year          the calendar year
 * @param allowedDays   the annual allowance
 * @param usedDays      approved days taken this year
 * @param remainingDays allowance minus used
 */
public record LeaveBalanceResponse(
        int year,
        int allowedDays,
        long usedDays,
        long remainingDays
) {
}
