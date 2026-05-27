package com.springlms.backend.dto;

public record StudentAttendanceSummaryResponse(
        Double attendancePercentage,
        Double attendedEquivalent,
        Integer totalExpectedSessions,
        Integer recordedSessions,
        Integer remainingLectureCount,
        Double remainingAbsenceEquivalentUntilFinalExam,
        Integer minimumAttendancePercentForFinalExam,
        Integer semesterWeeks
) {
}
