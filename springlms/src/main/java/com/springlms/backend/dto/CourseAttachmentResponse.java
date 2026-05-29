package com.springlms.backend.dto;

public record CourseAttachmentResponse(
        Long id,
        Long sectionId,
        String sectionCode,
        String courseName,
        String title,
        String attachmentType,
        String resourceUrl,
        String description,
        String teacherName,
        String createdAt
) {
}
