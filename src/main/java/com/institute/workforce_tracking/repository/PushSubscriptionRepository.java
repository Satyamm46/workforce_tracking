package com.institute.workforce_tracking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.institute.workforce_tracking.entity.PushSubscription;

/**
 * Data-access layer for {@link PushSubscription} rows.
 */
@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    /** All browsers subscribed for one user. */
    List<PushSubscription> findByUserId(Long userId);

    /** Looks a subscription up by its unique push-service endpoint. */
    Optional<PushSubscription> findByEndpoint(String endpoint);
}
