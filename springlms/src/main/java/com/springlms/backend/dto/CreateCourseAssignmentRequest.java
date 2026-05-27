package com.springlms.backend.dto;

public record CreateCourseAssignmentRequest(
        Long sectionId,
        String title,
        String description,
        String dueDate,
        Integer maxScore
) {
}
