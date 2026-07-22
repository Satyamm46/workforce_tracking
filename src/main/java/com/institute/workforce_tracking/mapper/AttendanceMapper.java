package com.institute.workforce_tracking.mapper;

import java.time.Duration;

import org.springframework.stereotype.Component;

import com.institute.workforce_tracking.dto.response.AttendanceResponse;
import com.institute.workforce_tracking.entity.Attendance;
import com.institute.workforce_tracking.util.DateTimeUtil;

/**
 * Converts {@link Attendance} entities to their outbound representation.
 */
@Component
public class AttendanceMapper {

    /**
     * Maps an attendance record, computing the "worked so far" figure for
     * in-progress days.
     *
     * <p>Callers must ensure the record's user is loadable — either already in
     * the persistence context ("my" views) or pre-fetched via the repository's
     * entity graph (admin views).</p>
     */
    public AttendanceResponse toAttendanceResponse(Attendance attendance) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getUser().getId(),
                attendance.getUser().getFullName(),
                attendance.getWorkDate(),
                attendance.getLoginTime(),
                attendance.getLogoutTime(),
                attendance.getTotalBreakMinutes(),
                calculateWorkingMinutes(attendance),
                attendance.getStatus(),
                attendance.isLateArrival(),
                attendance.isHalfDay(),
                attendance.isAbsentNoReport(),
                attendance.getOvertimeDeadline()
        );
    }

    /**
     * Final stored minutes once clocked out; otherwise a live snapshot:
     * (now − login) − breaks, floored at zero.
     */
    private long calculateWorkingMinutes(Attendance attendance) {
        if (attendance.getWorkingMinutes() != null) {
            return attendance.getWorkingMinutes();
        }
        if (attendance.getLoginTime() == null) {
            return 0; // generated leave record — no working time
        }
        long elapsedMinutes = Duration
                .between(attendance.getLoginTime(), DateTimeUtil.now())
                .toMinutes();
        return Math.max(elapsedMinutes - attendance.getTotalBreakMinutes(), 0);
    }

        
    }