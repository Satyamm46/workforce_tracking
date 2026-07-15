package com.institute.workforce_tracking.service.impl;

import com.institute.workforce_tracking.dto.response.NotificationResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.entity.Notification;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.enums.NotificationType;
import com.institute.workforce_tracking.exception.ResourceNotFoundException;
import com.institute.workforce_tracking.mapper.NotificationMapper;
import com.institute.workforce_tracking.repository.NotificationRepository;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.service.NotificationService;
import com.institute.workforce_tracking.util.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link NotificationService}.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    /** The user-destination suffix clients subscribe to. */
    private static final String NOTIFICATION_QUEUE = "/queue/notifications";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository,
                                   NotificationMapper notificationMapper,
                                   SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationMapper = notificationMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public void notifyUser(Long userId, String email, NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setUser(userRepository.getReferenceById(userId));
        notification.setType(type);
        notification.setMessage(message);
        Notification saved = notificationRepository.save(notification);

        // Best-effort live delivery: the row above is the source of truth, so a
        // push failure (user offline, broker hiccup) must not fail creation.
        try {
            messagingTemplate.convertAndSendToUser(
                    email, NOTIFICATION_QUEUE, notificationMapper.toNotificationResponse(saved));
        } catch (Exception ex) {
            log.warn("WebSocket push failed for user {}: {}", email, ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getMyNotifications(String email, int page, int size) {
        User user = findUserByEmail(email);
        Pageable pageable = PageRequest.of(PageUtils.safePage(page), PageUtils.safeSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NotificationResponse> result = notificationRepository.findByUser(user, pageable)
                .map(notificationMapper::toNotificationResponse);
        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public long getMyUnreadCount(String email) {
        return notificationRepository.countByUserAndReadFalse(findUserByEmail(email));
    }

    @Override
    @Transactional
    public void markAllRead(String email) {
        notificationRepository.markAllRead(findUserByEmail(email));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
