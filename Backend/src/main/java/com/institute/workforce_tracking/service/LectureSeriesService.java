package com.institute.workforce_tracking.service;

import java.util.List;

import com.institute.workforce_tracking.dto.request.CreateLectureSeriesRequest;
import com.institute.workforce_tracking.dto.response.LectureSeriesResponse;

/**
 * Business operations for repeating lecture series. A series is a template
 * from which concrete {@link com.institute.workforce_tracking.entity.Lecture}
 * rows are generated on a rolling horizon; everything downstream (status
 * sweeps, summaries, reports) sees only ordinary lectures.
 */
public interface LectureSeriesService {

    /**
     * Creates a series for the authenticated teacher and immediately
     * materialises its occurrences through the rolling horizon. The response
     * reports how many lectures were created and which dates were skipped
     * because an existing lecture clashed.
     */
    LectureSeriesResponse createSeries(String teacherEmail, CreateLectureSeriesRequest request);

    /** The authenticated teacher's active series. */
    List<LectureSeriesResponse> getMySeries(String teacherEmail);

    /**
     * Stops one of the caller's own series: no further occurrences are
     * generated, and its future SCHEDULED occurrences are cancelled. Past
     * occurrences stay on record so reports remain honest.
     */
    LectureSeriesResponse stopSeries(String teacherEmail, Long seriesId);

    /**
     * Sweep: tops every active series up to the rolling horizon. Returns how
     * many lectures were created.
     */
    int materialiseUpcoming();
}
