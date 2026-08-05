package com.institute.workforce_tracking.dto.response;

import java.time.LocalDateTime;

import com.institute.workforce_tracking.enums.Role;

/**
 * One person behind a dashboard statistic.
 *
 * <p>{@code status} and {@code since} carry the context that makes the row
 * worth reading — "on a break since 14:20" answers an admin's actual question,
 * where a bare name does not. The timestamp is sent raw so the client formats
 * it in the same way as every other time in the app.</p>
 *
 * @param userId   the person's id
 * @param fullName the person's name
 * @param role     their role, so an admin can tell staff apart at a glance
 * @param status   what they are doing, e.g. "On break", or the lecture for a
 *                 teacher mid-class
 * @param since    when that state began (check-in, break start, checkout,
 *                 lecture start); null when there is no meaningful moment
 */
public record DashboardMemberResponse(
        Long userId,
        String fullName,
        Role role,
        String status,
        LocalDateTime since
) {
}
