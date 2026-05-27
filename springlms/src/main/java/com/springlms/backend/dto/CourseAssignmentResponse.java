package com.springlms.backend.dto;

public record CourseAssignmentResponse(
        Long id,
        Long sectionId,
        String sectionCode,
        String courseName,
        String title,
        String description,
        String dueDate,
        Integer maxScore,
        String teacherName,
        String createdAt
) {
}
