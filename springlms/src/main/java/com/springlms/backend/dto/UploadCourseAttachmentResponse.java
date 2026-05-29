package com.springlms.backend.dto;

public record UploadCourseAttachmentResponse(
        Long id,
        String title,
        String attachmentType,
        String fileName,
        String resourceUrl
) {
}
