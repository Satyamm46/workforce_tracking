package com.institute.workforce_tracking.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.enums.NotificationType;
import com.institute.workforce_tracking.enums.Role;
import com.institute.workforce_tracking.event.AttendanceAutoActionEvent;
import com.institute.workforce_tracking.event.LateArrivalEvent;
import com.institute.workforce_tracking.event.LeaveDecidedEvent;
import com.institute.workforce_tracking.event.LectureEndingSoonEvent;
import com.institute.workforce_tracking.event.LectureMissedEvent;
import com.institute.workforce_tracking.event.LectureStartingSoonEvent;
import com.institute.workforce_tracking.event.RegistrationSubmittedEvent;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.service.EmailService;
import com.institute.workforce_tracking.service.NotificationService;

/**
 * Translates domain events into user notifications.
 *
 * <p>Listeners run AFTER the publishing transaction commits
 * ({@code @TransactionalEventListener}): a notification must describe a fact
 * that actually happened — and a notification failure must never roll back
 * the business action it announces. Each handler is additionally fail-safe.</p>
 */
@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public NotificationEventListener(NotificationService notificationService,
                                     UserRepository userRepository,
                                     EmailService emailService) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLectureEndingSoon(LectureEndingSoonEvent event) {
        try {
            notificationService.notifyUser(
                    event.teacherId(), event.teacherEmail(), NotificationType.LECTURE_ENDING,
                    "Your " + event.subject() + " lecture for " + event.className()
                            + " is going to end at " + event.effectiveEndTime()
                            + ". Do you want to extend the session?");
        } catch (Exception ex) {
            log.error("Failed to create lecture-ending notification", ex);
        }
    }

    /** Tells the teacher their never-started class was cancelled, with the fix. */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLectureMissed(LectureMissedEvent event) {
        try {
            notificationService.notifyUser(
                    event.teacherId(), event.teacherEmail(), NotificationType.LECTURE_MISSED,
                    "Your " + event.subject() + " lecture for " + event.className()
                            + " was cancelled because it was not started in its scheduled time. "
                            + "Use the Reschedule button on My Lectures to pick a new slot.");
        } catch (Exception ex) {
            log.error("Failed to create lecture-missed notification", ex);
        }
    }

    /** Heads-up to the teacher ~5 minutes before their scheduled start. */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLectureStartingSoon(LectureStartingSoonEvent event) {
        try {
            notificationService.notifyUser(
                    event.teacherId(), event.teacherEmail(), NotificationType.LECTURE_STARTING,
                    "Your " + event.subject() + " lecture for " + event.className()
                            + " starts at " + event.scheduledStart()
                            + ". Press Start Class when you begin.");
        } catch (Exception ex) {
            log.error("Failed to create lecture-starting notification", ex);
        }
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLeaveDecided(LeaveDecidedEvent event) {
        try {
            NotificationType type = event.approved()
                    ? NotificationType.LEAVE_APPROVED : NotificationType.LEAVE_REJECTED;
            String verdict = event.approved() ? "approved" : "rejected";
            notificationService.notifyUser(
                    event.userId(), event.email(), type,
                    "Your leave request (" + event.startDate() + " to " + event.endDate()
                            + ") was " + verdict + ".");
        } catch (Exception ex) {
            log.error("Failed to create leave-decision notification", ex);
        }
    }

    /**
     * Tells a user the system acted on their attendance: placed on break
     * after disconnecting, or checked out after the break limit. Delivered
     * in-app (visible on next login) and via push/WhatsApp through the
     * notification service's fan-out.
     */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAttendanceAutoAction(AttendanceAutoActionEvent event) {
        try {
            NotificationType type = event.checkedOut()
                    ? NotificationType.AUTO_CHECKED_OUT : NotificationType.AUTO_BREAK_STARTED;
            String message = event.checkedOut()
                    ? "You were checked out automatically because your break exceeded the limit."
                    : "You were placed on a break automatically after disconnecting without logging out.";
            notificationService.notifyUser(event.userId(), event.email(), type, message);
        } catch (Exception ex) {
            log.error("Failed to create attendance auto-action notification", ex);
        }
    }

    /** Tells the user their late check-in made today a half day. */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLateArrival(LateArrivalEvent event) {
        try {
            notificationService.notifyUser(
                    event.userId(), event.email(), NotificationType.LATE_ARRIVAL,
                    "You checked in " + event.minutesLate() + " minutes after your planned start ("
                            + event.plannedStart() + "). Today is counted as a half day.");
        } catch (Exception ex) {
            log.error("Failed to create late-arrival notification", ex);
        }
    }

    /**
     * Alerts every Super Admin — in-app and by email — that a new
     * self-registration is awaiting their decision.
     */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onRegistrationSubmitted(RegistrationSubmittedEvent event) {
        String message = event.fullName() + " (" + event.email() + ") requested to join as "
                + event.requestedRole() + " and is awaiting your approval.";

        for (User admin : userRepository.findByRole(Role.SUPER_ADMIN)) {
            try {
                notificationService.notifyUser(
                        admin.getId(), admin.getEmail(),
                        NotificationType.REGISTRATION_SUBMITTED, message);
            } catch (Exception ex) {
                log.error("Failed to create registration notification for {}", admin.getEmail(), ex);
            }

            // EmailService is itself fail-safe and async.
            emailService.send(admin.getEmail(),
                    "New registration request: " + event.fullName(),
                    message + "\n\nReview it in the Registrations screen of the admin panel.");
        }
    }
}
