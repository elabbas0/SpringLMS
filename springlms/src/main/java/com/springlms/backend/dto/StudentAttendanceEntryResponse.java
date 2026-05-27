package com.springlms.backend.dto;

import com.springlms.backend.model.attendance.AttendanceStatus;

public record StudentAttendanceEntryResponse(
        Long attendanceSessionId,
        Long scheduleEntryId,
        String sessionDate,
        String dayOfWeek,
        String subjectName,
        String sessionType,
        String teacherName,
        AttendanceStatus status
) {
}
