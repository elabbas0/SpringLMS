package com.springlms.backend.dto;

public record StudentGroupTeacherAssignmentResponse(
        Long id,
        Long teacherUserId,
        String teacherEmail,
        String teacherName,
        String subjectName,
        Integer creditValue
) {
}
