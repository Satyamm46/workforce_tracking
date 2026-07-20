package com.institute.workforce_tracking.service.impl;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.request.PushSubscriptionRequest;
import com.institute.workforce_tracking.entity.PushSubscription;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.exception.ResourceNotFoundException;
import com.institute.workforce_tracking.repository.PushSubscriptionRepository;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.service.PushService;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushAsyncService;
import nl.martijndwars.webpush.Subscription;

/**
 * VAPID Web Push implementation of {@link PushService}.
 *
 * <p>Disabled (all sends skipped with a log line) until the VAPID key pair is
 * configured. Subscriptions rejected by the push service with 404/410 are
 * pruned — the browser unsubscribed or the subscription expired.</p>
 */
@Service
public class PushServiceImpl implements PushService {

    private static final Logger log = LoggerFactory.getLogger(PushServiceImpl.class);

    private final PushSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final String publicKey;
    private final PushAsyncService pushAsyncService;

    public PushServiceImpl(PushSubscriptionRepository subscriptionRepository,
                           UserRepository userRepository,
                           @Value("${app.push.vapid.public-key:}") String publicKey,
                           @Value("${app.push.vapid.private-key:}") String privateKey,
                           @Value("${app.push.vapid.subject:mailto:admin@institute.com}") String subject) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.publicKey = publicKey;
        this.pushAsyncService = buildService(publicKey, privateKey, subject);
    }

    /** Builds the sender, or null when keys are absent/invalid (push disabled). */
    private static PushAsyncService buildService(String publicKey, String privateKey, String subject) {
        if (publicKey == null || publicKey.isBlank() || privateKey == null || privateKey.isBlank()) {
            log.info("Web Push disabled — VAPID keys not configured (app.push.vapid.*).");
            return null;
        }
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            return new PushAsyncService(publicKey, privateKey, subject);
        } catch (Exception ex) {
            log.error("Web Push disabled — invalid VAPID configuration: {}", ex.getMessage());
            return null;
        }
    }

    @Override
    public String getPublicKey() {
        return publicKey == null ? "" : publicKey;
    }

    @Override
    @Transactional
    public void subscribe(String email, PushSubscriptionRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Same endpoint re-registered (page reload, new login) → refresh keys
        // and owner rather than violating the unique constraint.
        PushSubscription subscription = subscriptionRepository.findByEndpoint(request.endpoint())
                .orElseGet(PushSubscription::new);
        subscription.setUser(user);
        subscription.setEndpoint(request.endpoint());
        subscription.setP256dh(request.p256dh());
        subscription.setAuth(request.auth());
        subscriptionRepository.save(subscription);

        log.info("Push subscription registered for {}", email);
    }

    @Override
    @Transactional
    public void unsubscribe(String email, String endpoint) {
        subscriptionRepository.findByEndpoint(endpoint)
                .ifPresent(subscriptionRepository::delete);
    }

    @Override
    @Async
    @Transactional
    public void sendToUser(Long userId, String title, String body) {
        if (pushAsyncService == null) {
            return; // push not configured — silently skipped
        }

        String payload = "{\"title\":" + jsonString(title) + ",\"body\":" + jsonString(body) + "}";

        for (PushSubscription stored : subscriptionRepository.findByUserId(userId)) {
            try {
                Subscription subscription = new Subscription(
                        stored.getEndpoint(),
                        new Subscription.Keys(stored.getP256dh(), stored.getAuth()));

                int status = pushAsyncService.send(new Notification(subscription, payload))
                        .get().getStatusCode();

                if (status == 404 || status == 410) {
                    // Browser unsubscribed / subscription expired — prune it.
                    subscriptionRepository.delete(stored);
                    log.info("Pruned dead push subscription for user {}", userId);
                } else if (status >= 400) {
                    log.warn("Push send for user {} returned HTTP {}", userId, status);
                }
            } catch (Exception ex) {
                log.warn("Push send failed for user {}: {}", userId, ex.getMessage());
            }
        }
    }

    /** Minimal JSON string escaping for the notification payload. */
    private static String jsonString(String value) {
        String escaped = value == null ? "" : value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
        return '"' + escaped + '"';
    }
}
