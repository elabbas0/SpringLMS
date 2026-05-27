package com.springlms.backend.dto;

public record ScheduleEntryResponse(
        Long id,
        Long studentGroupId,
        String studentGroupName,
        Integer studyYear,
        Long teacherUserId,
        String teacherEmail,
        String teacherName,
        String subjectName,
        String sessionType,
        String dayOfWeek,
        String startTime,
        String endTime
) {
}
