package com.springlms.backend.dto;

public record SpecializationResponse(
        Long id,
        String name,
        Long facultyId,
        String facultyName,
        Long teacherUserId,
        String teacherEmail,
        Integer semesterCreditTarget,
        Boolean archived
) {
}
