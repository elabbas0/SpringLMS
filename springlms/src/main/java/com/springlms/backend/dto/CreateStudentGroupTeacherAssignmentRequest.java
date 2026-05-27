package com.springlms.backend.dto;

public record CreateStudentGroupTeacherAssignmentRequest(
        Long teacherUserId,
        String subjectName,
        Integer creditValue,
        Integer lectureSessionsPerWeek,
        Integer seminarSessionsPerWeek,
        Integer labSessionsPerWeek
) {
}
