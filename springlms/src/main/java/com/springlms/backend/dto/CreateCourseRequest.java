package com.springlms.backend.dto;

public record CreateCourseRequest(
        String code,
        String name,
        String description,
        Integer credits
) {
}
