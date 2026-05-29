package com.springlms.backend.dto;

import java.util.List;

public record AttendanceSessionResponse(
        Long id,
        Long scheduleEntryId,
        Long studentGroupId,
        String studentGroupName,
        String subjectName,
        String sessionType,
        String dayOfWeek,
        String startTime,
        String endTime,
        String sessionDate,
        Boolean submitted,
        Boolean locked,
        String submittedAt,
        List<AttendanceRecordResponse> records
) {
}
