package com.institute.workforce_tracking.mapper;

import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.dto.response.LectureSummaryResponse;
import com.institute.workforce_tracking.entity.Lecture;
import com.institute.workforce_tracking.entity.LectureSummary;

@Component
public class LectureSummaryMapper {

    public LectureSummaryResponse toLectureSummaryResponse(LectureSummary summary) {
        Lecture lecture = summary.getLecture();
        boolean late = summary.isSubmittedLate();

        return new LectureSummaryResponse(
                summary.getId(),
                lecture.getId(),
                lecture.getTeacher().getFullName(),
                lecture.getSubject(),
                lecture.getClassName(),
                lecture.getLectureDate(),
                lecture.getStartTime(),
                lecture.getEndTime(),
                summary.getSummaryText(),
                summary.getSubmittedAt(),
                summary.getLectureEndTime(),
                late);
    }
}
