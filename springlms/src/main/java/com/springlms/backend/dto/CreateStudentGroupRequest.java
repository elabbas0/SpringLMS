package com.springlms.backend.dto;

public record CreateStudentGroupRequest(
        String name,
        Long specializationId
) {
}
