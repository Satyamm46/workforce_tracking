package com.institute.workforce_tracking.controller;

import com.institute.workforce_tracking.constants.ApiConstants;
import com.institute.workforce_tracking.dto.response.NotificationResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.response.ApiResponse;
import com.institute.workforce_tracking.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for the caller's notifications. Live delivery happens over
 * WebSocket; these endpoints own history, unread state, and read receipts.
 */
@RestController
@RequestMapping(ApiConstants.NOTIFICATIONS_BASE)
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** A page of the caller's notifications, newest first. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getMyNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<NotificationResponse> notifications =
                notificationService.getMyNotifications(authentication.getName(), page, size);
        return ResponseEntity.ok(ApiResponse.of("Notifications retrieved", notifications));
    }

    /** The caller's unread count (drives the bell badge). */
    @GetMapping("/me/unread-count")
    public ResponseEntity<ApiResponse<Long>> getMyUnreadCount(Authentication authentication) {
        long count = notificationService.getMyUnreadCount(authentication.getName());
        return ResponseEntity.ok(ApiResponse.of("Unread count retrieved", count));
    }

    /** Marks all of the caller's notifications as read. */
    @PatchMapping("/me/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(Authentication authentication) {
        notificationService.markAllRead(authentication.getName());
        return ResponseEntity.ok(ApiResponse.message("All notifications marked as read"));
    }
}
