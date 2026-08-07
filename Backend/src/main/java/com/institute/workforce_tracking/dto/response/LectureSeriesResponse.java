package com.institute.workforce_tracking.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import com.institute.workforce_tracking.enums.RecurrenceFrequency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A lecture series as returned to clients.
 *
 * <p>{@code occurrencesCreated} and {@code skippedDates} describe the most
 * recent materialisation touching this series — on create, that is the
 * immediate generation, so the teacher sees at once how many classes were
 * scheduled and which dates were skipped over conflicts. On plain reads both
 * are empty rather than null.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureSeriesResponse {

    private Long id;
    private String subject;
    private String className;
    private String batch;
    private LocalTime startTime;
    private LocalTime endTime;
    private RecurrenceFrequency frequency;

    /** Weekdays of a WEEKLY series; empty for MONTHLY. */
    private Set<DayOfWeek> weekdays;

    /** Day of the month of a MONTHLY series; null for WEEKLY. */
    private Integer dayOfMonth;

    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    /** Occurrences generated so far reach this date. */
    private LocalDate materializedThrough;

    /** Lectures created by the materialisation this response describes. */
    private int occurrencesCreated;

    /** Dates skipped because an existing lecture clashed (ISO strings). */
    private List<String> skippedDates;
}
