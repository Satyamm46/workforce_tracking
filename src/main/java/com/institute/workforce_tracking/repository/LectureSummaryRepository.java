package com.institute.workforce_tracking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.institute.workforce_tracking.entity.Lecture;
import com.institute.workforce_tracking.entity.LectureSummary;
import com.institute.workforce_tracking.entity.User;

public interface LectureSummaryRepository extends JpaRepository<LectureSummary, Long> {

    Optional<LectureSummary> findByLecture(Lecture lecture);

    boolean existsByLecture(Lecture lecture);

    @Query("SELECT ls FROM LectureSummary ls JOIN ls.lecture l WHERE l.teacher = :teacher")
    Page<LectureSummary> findByTeacher(User teacher, Pageable pageable);

    @Query("SELECT ls FROM LectureSummary ls JOIN ls.lecture l WHERE l.lectureDate = :date")
    Page<LectureSummary> findByLectureDate(java.time.LocalDate date, Pageable pageable);

    /** All summaries whose lecture date falls within [start, end] — backs the monthly export. */
    @Query("SELECT ls FROM LectureSummary ls JOIN ls.lecture l "
            + "WHERE l.lectureDate BETWEEN :start AND :end")
    Page<LectureSummary> findByLectureDateBetween(
            java.time.LocalDate start, java.time.LocalDate end, Pageable pageable);
}
