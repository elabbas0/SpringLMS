package com.springlms.backend.dto;

import com.springlms.backend.model.course.AttachmentType;

public record CreateCourseAttachmentRequest(
        Long sectionId,
        String title,
        AttachmentType attachmentType,
        String resourceUrl,
        String description
) {
}
