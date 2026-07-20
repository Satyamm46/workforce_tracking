package com.institute.workforce_tracking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One browser's Web Push subscription for a user.
 *
 * <p>A user can have several (phone + laptop, multiple browsers). The endpoint
 * URL is unique per subscription and is issued by the browser's push service;
 * {@code p256dh} and {@code auth} are the client keys needed to encrypt the
 * payload for that specific browser.</p>
 */
@Entity
@Table(name = "push_subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class PushSubscription extends BaseEntity {

    /** The user this browser belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Push-service URL for this browser. Unique — one row per subscription. */
    @Column(nullable = false, length = 512, unique = true)
    private String endpoint;

    /** Client public key (P-256 ECDH) for payload encryption. */
    @Column(nullable = false, length = 255)
    private String p256dh;

    /** Client auth secret for payload encryption. */
    @Column(nullable = false, length = 255)
    private String auth;
}
