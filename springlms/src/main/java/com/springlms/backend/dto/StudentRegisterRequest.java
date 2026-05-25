package com.springlms.backend.dto;

import com.springlms.backend.model.user.StudentFundingType;

import java.time.LocalDate;

public record StudentRegisterRequest(
        String email,
        String password,
        String finCode,
        String serialNumber,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phoneNumber,
        String address,
        Double entranceScore,
        Integer studyYear,
        Integer admissionYear,
        StudentFundingType fundingType,
        String emergencyContactName,
        String emergencyContactPhone,
        String emergencyContactRelation
) {
}
