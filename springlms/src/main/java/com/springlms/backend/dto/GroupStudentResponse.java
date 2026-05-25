package com.springlms.backend.dto;

import com.springlms.backend.model.user.State;
import com.springlms.backend.model.user.StudentFundingType;

public record GroupStudentResponse(
        Long userId,
        String email,
        String firstName,
        String lastName,
        State state,
        Double entranceScore,
        Integer studyYear,
        Integer admissionYear,
        StudentFundingType fundingType
) {
}
