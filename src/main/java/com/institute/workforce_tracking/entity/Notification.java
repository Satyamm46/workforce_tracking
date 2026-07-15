package com.institute.workforce_tracking.entity;

import com.institute.workforce_tracking.enums.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One in-app notification for one user.
 *
 * <p>Persisted rows are the source of truth for notification history and
 * unread counts; the WebSocket push is only a live delivery of the same data.
 * {@code createdAt} from {@link BaseEntity} doubles as the display timestamp.</p>
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification extends BaseEntity {

    /** The recipient. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Category, used for icons/routing. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    /** The human-readable message shown to the user. */
    @Column(nullable = false, length = 500)
    private String message;

    /**
     * Whether the user has seen this notification. Column named "is_read"
     * because READ is a reserved word in MySQL.
     */
    @Column(name = "is_read", nullable = false)
    private boolean read = false;
}
