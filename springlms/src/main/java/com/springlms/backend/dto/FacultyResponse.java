package com.springlms.backend.dto;

public record FacultyResponse(
        Long id,
        String name,
        String code,
        Boolean archived
) {
}
