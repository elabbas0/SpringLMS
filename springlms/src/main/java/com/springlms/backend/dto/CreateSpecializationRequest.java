package com.springlms.backend.dto;

public record CreateSpecializationRequest(
        String name,
        Long facultyId,
        Long teacherUserId
) {
}
