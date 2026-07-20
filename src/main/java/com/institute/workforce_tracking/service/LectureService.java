package com.institute.workforce_tracking.service;

import java.time.LocalDate;

import com.institute.workforce_tracking.dto.request.ScheduleLectureRequest;
import com.institute.workforce_tracking.dto.response.LectureResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;

/**
 * Business operations for lecture scheduling. Teachers manage their own
 * schedule; admins view all lectures by day. Lifecycle transitions beyond
 * cancellation (live, complete, extend) belong to the tracking milestone.
 */
public interface LectureService {

    /**
     * Schedules a new lecture for the authenticated teacher.
     */
    LectureResponse scheduleLecture(String teacherEmail, ScheduleLectureRequest request);

    /**
     * A page of the authenticated teacher's lectures from today onward.
     */
    PagedResponse<LectureResponse> getMyUpcomingLectures(String teacherEmail, int page, int size);

    /**
     * Cancels one of the caller's own lectures, if it has not started.
     */
    LectureResponse cancelLecture(String teacherEmail, Long lectureId);

    /**
     * Admin view: all lectures on the given day.
     */
    PagedResponse<LectureResponse> getLecturesByDate(LocalDate date, int page, int size);

        /** Ends one of the caller's own live lectures. */
    LectureResponse endLecture(String teacherEmail, Long lectureId);

    /**
     * Starts one of the caller's own scheduled lectures. The class keeps its
     * planned length from this moment: starting late shifts the whole
     * session, so the effective end is recalculated from the actual start.
     */
    LectureResponse startLecture(String teacherEmail, Long lectureId);

    /** Extends one of the caller's own live lectures (cumulative cap 30 min). */
    LectureResponse extendLecture(String teacherEmail, Long lectureId,
                                  com.institute.workforce_tracking.dto.request.ExtendLectureRequest request);

    /**
     * Reschedules one of the caller's own missed or cancelled lectures to a
     * new slot: a fresh SCHEDULED lecture is created with the same subject,
     * class, and batch; the original stays on record.
     */
    LectureResponse rescheduleLecture(String teacherEmail, Long lectureId,
                                      com.institute.workforce_tracking.dto.request.RescheduleLectureRequest request);

    /**
     * Sweep: publishes starting-soon reminders for scheduled lectures nearing
     * their start. Returns how many.
     */
    int publishStartReminders();

    /**
     * Sweep: completes live lectures past their effective end, and marks
     * never-started lectures past their scheduled end as MISSED. Returns how
     * many were affected.
     */
    int completeOverdueLectures();

    /** Sweep: publishes ending-soon events for live lectures nearing their end. Returns how many. */
    int publishEndingReminders();

}
