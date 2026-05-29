package com.springlms.backend.dto;

import java.util.List;

public record CourseSectionResponse(
        Long id,
        Long courseId,
        String courseCode,
        String courseName,
        String sectionCode,
        String termName,
        List<CourseSectionGroupResponse> groups,
        List<CourseSectionTeacherAssignmentResponse> teacherAssignments,
        List<CourseAttachmentResponse> attachments,
        Integer enrollmentCount
) {
}
