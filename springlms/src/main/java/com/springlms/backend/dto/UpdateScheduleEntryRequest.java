package com.springlms.backend.dto;

public record UpdateScheduleEntryRequest(
        String dayOfWeek,
        String startTime
) {
}
