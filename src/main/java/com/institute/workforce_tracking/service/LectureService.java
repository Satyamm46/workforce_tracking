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
}
