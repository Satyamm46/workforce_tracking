package com.institute.workforce_tracking.entity;

import java.time.LocalDateTime;

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
 * A teacher's post-lecture summary, required within 24 hours of the lecture
 * ending. Missing summaries trigger lecture cancellation.
 */
@Entity
@Table(name = "lecture_summaries", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"lecture_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LectureSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Column(nullable = false, length = 2000)
    private String summaryText;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    /** The lecture's actual end time — deadline is this + 24h. */
    @Column(nullable = false)
    private LocalDateTime lectureEndTime;

    /**
     * Whether this summary arrived after its effective deadline (base 24h
     * plus any admin-granted extension). Computed and stored at submit time,
     * when the extension is known.
     */
    @Column(nullable = false)
    @org.hibernate.annotations.ColumnDefault("false")
    private boolean submittedLate = false;
}
