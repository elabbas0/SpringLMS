package com.springlms.backend.dto;

import java.time.LocalDate;

public record CreateTeacherRequest(
        String email,
        String password,
        String finCode,
        String serialNumber,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phoneNumber,
        String address,
        Long facultyId,
        Integer hoursTaught
) {
}
