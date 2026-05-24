package com.springlms.backend.dto;

import com.springlms.backend.model.user.State;

public record TeacherStudentOptionResponse(
        Long userId,
        String email,
        String firstName,
        String lastName,
        State state,
        String currentGroupName
) {
}
