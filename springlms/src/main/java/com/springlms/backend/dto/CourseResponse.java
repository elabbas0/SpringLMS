package com.springlms.backend.dto;

public record CourseResponse(
        Long id,
        String code,
        String name,
        String description,
        Integer credits,
        Boolean archived
) {
}
