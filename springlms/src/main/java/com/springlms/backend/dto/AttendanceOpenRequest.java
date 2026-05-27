package com.springlms.backend.dto;

public record AttendanceOpenRequest(
        Long scheduleEntryId,
        String sessionDate
) {
}
