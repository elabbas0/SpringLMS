package com.springlms.backend.dto;

import java.util.List;

public record CreateStudentGroupRequest(
        String name,
        Long specializationId,
        Integer studyYear,
        List<CreateStudentGroupTeacherAssignmentRequest> teacherAssignments
) {
}
