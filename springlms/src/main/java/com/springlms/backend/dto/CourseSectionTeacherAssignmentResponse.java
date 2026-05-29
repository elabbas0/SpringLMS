package com.springlms.backend.dto;

public record CourseSectionTeacherAssignmentResponse(
        Long id,
        Long teacherUserId,
        String teacherEmail,
        String teacherName,
        String assignmentType
) {
}
