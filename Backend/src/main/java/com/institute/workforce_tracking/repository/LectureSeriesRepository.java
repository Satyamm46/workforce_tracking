package com.institute.workforce_tracking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.institute.workforce_tracking.entity.LectureSeries;
import com.institute.workforce_tracking.entity.User;

/**
 * Data-access layer for {@link LectureSeries} templates.
 */
@Repository
public interface LectureSeriesRepository extends JpaRepository<LectureSeries, Long> {

    /** One teacher's still-running series, for their management list. */
    List<LectureSeries> findByTeacherAndActiveTrueOrderByStartTimeAsc(User teacher);

    /**
     * Every running series, for the nightly materialisation sweep. The teacher
     * is pre-fetched because generation needs it for conflict detection.
     */
    @EntityGraph(attributePaths = "teacher")
    List<LectureSeries> findByActiveTrue();
}
