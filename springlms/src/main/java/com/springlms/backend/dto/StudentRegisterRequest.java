package com.springlms.backend.dto;

import java.time.LocalDate;

public record StudentRegisterRequest(
        String email,
        String password,
        String studentNumber,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phoneNumber,
        String address,
        String emergencyContactName,
        String emergencyContactPhone,
        String emergencyContactRelation
) {
}