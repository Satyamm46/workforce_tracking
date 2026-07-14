package com.institute.workforce_tracking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.institute.workforce_tracking.entity.Attendance;
import com.institute.workforce_tracking.entity.WorkBreak;

/**
 * Data-access layer for {@link WorkBreak} records.
 */
@Repository
public interface WorkBreakRepository extends JpaRepository<WorkBreak, Long> {

    /**
     * Finds the currently open break (no end time) for an attendance record.
     *
     * <p>Backs the resume-work operation: the open break is the one to close.
     * At most one can exist, guaranteed by the status state machine.</p>
     *
     * @param attendance the working day to look in
     * @return the open break, if the employee is currently on one
     */
    Optional<WorkBreak> findByAttendanceAndEndTimeIsNull(Attendance attendance);
}