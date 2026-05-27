package com.springlms.backend.dto;

import java.util.List;

public record AttendanceSaveRequest(
        List<AttendanceRecordUpdateRequest> records,
        Boolean submitted
) {
}
