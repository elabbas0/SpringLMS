package com.springlms.backend.dto;

import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.State;
import com.springlms.backend.model.user.StudentFundingType;

public record UserResponse(
        Long id,
        String email,
        Role role,
        State state,
        String firstName,
        String lastName,
        Double entranceScore,
        Integer studyYear,
        Integer admissionYear,
        StudentFundingType fundingType
) {
}
