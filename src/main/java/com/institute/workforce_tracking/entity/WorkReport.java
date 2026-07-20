package com.institute.workforce_tracking.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A user's end-of-day work report, required within 24 hours of checkout.
 * Missing reports trigger absence marking.
 */
@Entity
@Table(name = "work_reports", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "work_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false, length = 2000)
    private String reportText;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    /** The attendance row's checkout time — deadline is this + 24h. */
    @Column(nullable = false)
    private LocalDateTime checkoutTime;

    /**
     * The attendance row's first check-in (login) time for the day. Captured
     * here so a report shows the actual working window without re-joining
     * attendance. Null if the login time was somehow absent.
     */
    private LocalDateTime checkInTime;

    /**
     * The work schedule the user declared for this day — planned start/end
     * times and description, copied from their work plan at submit time.
     * Null when no plan existed (e.g. Super Admins, who are not required to
     * file one).
     */
    private LocalTime plannedStartTime;

    private LocalTime plannedEndTime;

    @Column(length = 1000)
    private String plannedWork;

    /**
     * Whether this report arrived after its effective deadline (base 24h
     * plus any admin-granted extension). Computed and stored at submit time,
     * when the extension is known.
     */
    @Column(nullable = false)
    @org.hibernate.annotations.ColumnDefault("false")
    private boolean submittedLate = false;
}
