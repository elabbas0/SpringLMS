package com.springlms.backend.dto;

import java.util.List;

public record StudentGroupResponse(
        Long id,
        String name,
        Long specializationId,
        String specializationName,
        Long facultyId,
        String facultyName,
        Integer studyYear,
        Boolean approved,
        Boolean archived,
        List<StudentGroupTeacherAssignmentResponse> teacherAssignments,
        List<GroupStudentResponse> students
) {
}
