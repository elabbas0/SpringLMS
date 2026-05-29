package com.springlms.backend.dto;

public record GradeCourseAssignmentSubmissionRequest(
        Integer score,
        String teacherFeedback
) {
}
