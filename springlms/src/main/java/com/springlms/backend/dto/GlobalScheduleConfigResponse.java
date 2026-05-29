package com.springlms.backend.dto;

public record GlobalScheduleConfigResponse(
        Double absenceValue,
        Double lateValue,
        Integer minimumAssignedGroupsForAutoSchedule,
        Integer lectureLengthMinutes,
        Integer semesterWeeks,
        Integer minimumAttendancePercentForFinalExam,
        String year1StartTime,
        String year1EndTime,
        String year2StartTime,
        String year2EndTime,
        String year3StartTime,
        String year3EndTime,
        String year4StartTime,
        String year4EndTime
) {
}
