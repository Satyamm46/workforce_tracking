package com.institute.workforce_tracking.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.entity.WorkReport;

public interface WorkReportRepository extends JpaRepository<WorkReport, Long> {

    Optional<WorkReport> findByUserAndWorkDate(User user, LocalDate workDate);

    Page<WorkReport> findByUser(User user, Pageable pageable);

    Page<WorkReport> findByWorkDate(LocalDate workDate, Pageable pageable);

    /** All reports whose work date falls within [start, end] — backs the monthly export. */
    Page<WorkReport> findByWorkDateBetween(LocalDate start, LocalDate end, Pageable pageable);

    boolean existsByUserAndWorkDate(User user, LocalDate workDate);
}
