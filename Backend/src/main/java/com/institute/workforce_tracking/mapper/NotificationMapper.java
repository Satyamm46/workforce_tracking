package com.institute.workforce_tracking.mapper;

import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.dto.response.NotificationResponse;
import com.institute.workforce_tracking.entity.Notification;

/**
 * Converts {@link Notification} entities to their outbound representation.
 */
@Component
public class NotificationMapper {

    public NotificationResponse toNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
