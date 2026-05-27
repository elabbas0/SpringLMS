package com.springlms.backend.dto;

import com.springlms.backend.model.attendance.AttendanceStatus;

public record AttendanceRecordUpdateRequest(
        Long studentUserId,
        AttendanceStatus status
) {
}
