package com.springlms.backend.dto;

import java.util.List;

public record CreateCourseSectionRequest(
        Long courseId,
        String sectionCode,
        List<Long> groupIds,
        List<CreateCourseSectionTeacherAssignmentRequest> teacherAssignments
) {
}
