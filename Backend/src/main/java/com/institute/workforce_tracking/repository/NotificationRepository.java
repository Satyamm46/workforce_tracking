package com.institute.workforce_tracking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.institute.workforce_tracking.entity.Notification;
import com.institute.workforce_tracking.entity.User;

/**
 * Data-access layer for {@link Notification} records.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** A page of one user's notifications (newest first via the Pageable). */
    Page<Notification> findByUser(User user, Pageable pageable);

    /** How many unread notifications the user has (drives the bell badge). */
    long countByUserAndReadFalse(User user);

    /**
     * Marks all of a user's unread notifications as read in ONE update
     * statement, without loading them.
     *
     * @return how many rows were updated
     */
    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.read = true
            WHERE n.user = :user AND n.read = false
            """)
    int markAllRead(@Param("user") User user);
}
