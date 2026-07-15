package com.institute.workforce_tracking.service;

import com.institute.workforce_tracking.dto.response.NotificationResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.enums.NotificationType;

/**
 * Business operations for in-app notifications: creation with real-time push
 * (called by event listeners), and the user-facing history/read operations.
 */
public interface NotificationService {

    /**
     * Persists a notification for a user and pushes it to their active
     * WebSocket sessions. The database row is the source of truth; the push
     * is best-effort.
     */
    void notifyUser(Long userId, String email, NotificationType type, String message);

    /** A page of the caller's notifications, newest first. */
    PagedResponse<NotificationResponse> getMyNotifications(String email, int page, int size);

    /** The caller's unread count (drives the bell badge). */
    long getMyUnreadCount(String email);

    /** Marks all of the caller's notifications as read. */
    void markAllRead(String email);
}
