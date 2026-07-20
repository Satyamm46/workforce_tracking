package com.institute.workforce_tracking.dto.response;

import java.time.LocalDate;

import com.institute.workforce_tracking.enums.DeadlineType;

/**
 * Outbound DTO for a granted deadline extension.
 *
 * @param id           extension primary key
 * @param userId       the user whose deadline was extended
 * @param userFullName that user's name
 * @param type         which deadline was extended
 * @param targetDate   the day the deadline belongs to
 * @param extraHours   grace hours added to the normal window
 * @param grantedBy    email of the granting admin
 * @param reason       optional reason
 */
public record DeadlineExtensionResponse(
        Long id,
        Long userId,
        String userFullName,
        DeadlineType type,
        LocalDate targetDate,
        int extraHours,
        String grantedBy,
        String reason
) {
}
