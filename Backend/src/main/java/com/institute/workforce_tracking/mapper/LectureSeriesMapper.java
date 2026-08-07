package com.institute.workforce_tracking.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.dto.response.LectureSeriesResponse;
import com.institute.workforce_tracking.entity.LectureSeries;

/**
 * Converts {@link LectureSeries} entities to their outbound representation.
 */
@Component
public class LectureSeriesMapper {

    /** Maps a series with no materialisation report (plain reads). */
    public LectureSeriesResponse toResponse(LectureSeries series) {
        return toResponse(series, 0, List.of());
    }

    /**
     * Maps a series along with the outcome of the materialisation that just
     * ran, so a create response can say what actually happened.
     */
    public LectureSeriesResponse toResponse(LectureSeries series,
                                            int occurrencesCreated,
                                            List<String> skippedDates) {
        return LectureSeriesResponse.builder()
                .id(series.getId())
                .subject(series.getSubject())
                .className(series.getClassName())
                .batch(series.getBatch())
                .startTime(series.getStartTime())
                .endTime(series.getEndTime())
                .frequency(series.getFrequency())
                .weekdays(series.getWeekdays())
                .dayOfMonth(series.getDayOfMonth())
                .startDate(series.getStartDate())
                .endDate(series.getEndDate())
                .active(series.isActive())
                .materializedThrough(series.getMaterializedThrough())
                .occurrencesCreated(occurrencesCreated)
                .skippedDates(skippedDates)
                .build();
    }
}
