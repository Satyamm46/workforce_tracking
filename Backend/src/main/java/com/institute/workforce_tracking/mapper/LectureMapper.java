package com.institute.workforce_tracking.mapper;

import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.dto.response.LectureResponse;
import com.institute.workforce_tracking.entity.Lecture;

/**
 * Converts {@link Lecture} entities to their outbound representation.
 *
 * <p>Must run inside a transaction (or on a page fetched with the teacher
 * entity graph): it reads the lazy {@code teacher} association.</p>
 */
@Component
public class LectureMapper {

    public LectureResponse toLectureResponse(Lecture lecture) {
        return new LectureResponse(
                lecture.getId(),
                lecture.getTeacher().getId(),
                lecture.getTeacher().getFullName(),
                lecture.getSubject(),
                lecture.getClassName(),
                lecture.getBatch(),
                lecture.getLectureDate(),
                lecture.getStartTime(),
                lecture.getEndTime(),
                lecture.getActualStartTime(),
                lecture.getExtendedMinutes(),
                lecture.getEffectiveEndTime(),
                lecture.getStatus()
        );
    }
}
