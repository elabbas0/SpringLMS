package com.springlms.backend.dto;

import com.springlms.backend.model.attendance.AttendanceStatus;

public record AttendanceRecordResponse(
        Long studentUserId,
        String email,
        String firstName,
        String lastName,
        AttendanceStatus status
) {
}
