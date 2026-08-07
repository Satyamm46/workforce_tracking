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
import com.institute.workforce_tracking.event.LecturesTomorrowEvent;
import com.institute.workforce_tracking.event.OvertimeCheckedOutEvent;
import com.institute.workforce_tracking.event.OvertimeReminderEvent;
import com.institute.workforce_tracking.event.RegistrationSubmittedEvent;
import com.institute.workforce_tracking.event.WorkEndReminderEvent;
import com.institute.workforce_tracking.event.WorkStartReminderEvent;
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
        String message = "Your " + event.subject() + " lecture for " + event.className()
                + " is going to end at " + event.effectiveEndTime()
                + ". Do you want to extend the session?";
        try {
            notificationService.notifyUser(
                    event.teacherId(), event.teacherEmail(),
                    NotificationType.LECTURE_ENDING, message);
        } catch (Exception ex) {
            log.error("Failed to create lecture-ending notification", ex);
        }
        // By email too, matching the starting-soon reminder: a teacher mid-class
        // is the least likely person to be watching the app.
        emailService.send(event.teacherEmail(), "Your lecture is about to end", message);
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
        String message = "Your " + event.subject() + " lecture for " + event.className()
                + " starts at " + event.scheduledStart() + ". Press Start Class when you begin.";
        try {
            notificationService.notifyUser(
                    event.teacherId(), event.teacherEmail(),
                    NotificationType.LECTURE_STARTING, message);
        } catch (Exception ex) {
            log.error("Failed to create lecture-starting notification", ex);
        }
        // Also by email — a class reminder should reach the teacher off-screen.
        emailService.send(event.teacherEmail(), "Your lecture starts soon", message);
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

    /** Heads-up before the user's declared work-start time — in-app and email. */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onWorkStartReminder(WorkStartReminderEvent event) {
        String message = "Your work day starts at " + event.plannedStart()
                + ". Remember to check in.";
        try {
            notificationService.notifyUser(
                    event.userId(), event.email(), NotificationType.WORK_START_REMINDER, message);
        } catch (Exception ex) {
            log.error("Failed to create work-start reminder", ex);
        }
        emailService.send(event.email(), "Your work day starts soon", message);
    }

    /** Heads-up before the user's declared work-end time — in-app and email. */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onWorkEndReminder(WorkEndReminderEvent event) {
        String message = "Your work day is going to end at " + event.plannedEnd()
                + ". Do you want to extend your work time, or check out?";
        try {
            notificationService.notifyUser(
                    event.userId(), event.email(), NotificationType.WORK_END_REMINDER, message);
        } catch (Exception ex) {
            log.error("Failed to create work-end reminder", ex);
        }
        emailService.send(event.email(), "Your work day is about to end", message);
    }

    /** Warns an employee their overtime window is closing — in-app and email. */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOvertimeReminder(OvertimeReminderEvent event) {
        String message = "You are in overtime. It ends at " + event.deadline().toLocalTime()
                + " — extend by 30 minutes or check out.";
        try {
            notificationService.notifyUser(
                    event.userId(), event.email(), NotificationType.OVERTIME_REMINDER, message);
        } catch (Exception ex) {
            log.error("Failed to create overtime reminder", ex);
        }
        emailService.send(event.email(), "Your overtime is about to end", message);
    }

    /** Tells the employee they were auto-checked-out after overtime lapsed. */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOvertimeCheckedOut(OvertimeCheckedOutEvent event) {
        try {
            notificationService.notifyUser(
                    event.userId(), event.email(), NotificationType.OVERTIME_CHECKED_OUT,
                    "You were checked out automatically because your overtime window "
                            + "closed without an extension.");
        } catch (Exception ex) {
            log.error("Failed to create overtime-checkout notification", ex);
        }
    }

    /**
     * Evening digest of tomorrow's classes — in-app and email.
     *
     * <p>The two channels carry different depth: the notification row is
     * capped at 500 characters, so the in-app text is one condensed line cut
     * with "…and N more" when needed, while the email lists every class in
     * full.</p>
     */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLecturesTomorrow(LecturesTomorrowEvent event) {
        int count = event.entries().size();
        String inApp = buildCondensedDigest(event, count);
        try {
            notificationService.notifyUser(
                    event.teacherId(), event.teacherEmail(),
                    NotificationType.LECTURE_TOMORROW, inApp);
        } catch (Exception ex) {
            log.error("Failed to create classes-tomorrow notification", ex);
        }

        StringBuilder body = new StringBuilder("Hi ").append(event.teacherFullName())
                .append(",\n\nYou have ").append(count)
                .append(count == 1 ? " class" : " classes")
                .append(" scheduled tomorrow (").append(event.date()).append("):\n\n");
        for (String entry : event.entries()) {
            body.append("  - ").append(entry).append('\n');
        }
        body.append("\nSee you in class!");
        emailService.send(event.teacherEmail(), "Your classes tomorrow", body.toString());
    }

    /** The in-app line, kept under the notification column's 500-char cap. */
    private static String buildCondensedDigest(LecturesTomorrowEvent event, int count) {
        StringBuilder text = new StringBuilder("You have ").append(count)
                .append(count == 1 ? " class" : " classes")
                .append(" tomorrow: ");
        int included = 0;
        for (String entry : event.entries()) {
            String next = (included == 0 ? "" : "; ") + entry;
            // Leave room for the worst-case "; …and NN more" suffix.
            if (text.length() + next.length() > 480) {
                break;
            }
            text.append(next);
            included++;
        }
        if (included < count) {
            text.append("; …and ").append(count - included).append(" more");
        }
        return text.toString();
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
