package com.springlms.backend.controller;

import com.springlms.backend.dto.StudentAttendanceSummaryResponse;
import com.springlms.backend.dto.StudentAttendanceEntryResponse;
import com.springlms.backend.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Student - Attendance", description = "Student endpoints for attendance summaries and history.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/student/attendance")
@RequiredArgsConstructor
public class StudentAttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/summary")
    @Operation(summary = "View attendance summary", description = "Returns attendance progress and final exam risk data.")
    public StudentAttendanceSummaryResponse getSummary(Authentication authentication) {
        return attendanceService.getStudentSummary(authentication.getName());
    }

    @GetMapping("/entries")
    @Operation(summary = "View attendance entries", description = "Returns the authenticated student's attendance history.")
    public java.util.List<StudentAttendanceEntryResponse> listEntries(Authentication authentication) {
        return attendanceService.listStudentAttendanceEntries(authentication.getName());
    }
}
