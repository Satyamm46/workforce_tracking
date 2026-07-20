package com.institute.workforce_tracking.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.institute.workforce_tracking.entity.DeadlineExtension;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.enums.DeadlineType;

public interface DeadlineExtensionRepository extends JpaRepository<DeadlineExtension, Long> {

    Optional<DeadlineExtension> findByUserAndTypeAndTargetDate(
            User user, DeadlineType type, LocalDate targetDate);

    @EntityGraph(attributePaths = "user")
    Page<DeadlineExtension> findAllByOrderByTargetDateDesc(Pageable pageable);
}
