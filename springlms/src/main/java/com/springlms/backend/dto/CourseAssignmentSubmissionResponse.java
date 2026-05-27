package com.springlms.backend.dto;

public record CourseAssignmentSubmissionResponse(
        Long id,
        Long assignmentId,
        String assignmentTitle,
        Long studentUserId,
        String studentName,
        String textContent,
        String fileName,
        String fileContentType,
        String linkUrl,
        Integer score,
        String teacherFeedback,
        String submittedAt,
        String gradedAt,
        String gradedBy
) {
}
