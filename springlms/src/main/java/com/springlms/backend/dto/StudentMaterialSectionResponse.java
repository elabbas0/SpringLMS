package com.springlms.backend.dto;

import java.util.List;

public record StudentMaterialSectionResponse(
        Long sectionId,
        String courseCode,
        String courseName,
        String sectionCode,
        List<CourseAttachmentResponse> attachments
) {
}
