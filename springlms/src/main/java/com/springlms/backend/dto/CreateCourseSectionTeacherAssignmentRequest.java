package com.springlms.backend.dto;

import com.springlms.backend.model.course.TeacherAssignmentType;

public record CreateCourseSectionTeacherAssignmentRequest(
        Long teacherUserId,
        TeacherAssignmentType assignmentType
) {
}
