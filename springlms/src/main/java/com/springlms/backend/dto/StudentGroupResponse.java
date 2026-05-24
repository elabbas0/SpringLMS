package com.springlms.backend.dto;

import java.util.List;

public record StudentGroupResponse(
        Long id,
        String name,
        Long specializationId,
        String specializationName,
        Long facultyId,
        String facultyName,
        Long teacherUserId,
        String teacherEmail,
        Boolean approved,
        Boolean archived,
        List<GroupStudentResponse> students
) {
}
