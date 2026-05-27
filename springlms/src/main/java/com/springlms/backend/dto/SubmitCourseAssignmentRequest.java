package com.springlms.backend.dto;

public record SubmitCourseAssignmentRequest(
        String textContent,
        String linkUrl
) {
}
