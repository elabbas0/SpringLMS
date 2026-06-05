package com.springlms.backend.controller;

import com.springlms.backend.dto.AttendanceOpenRequest;
import com.springlms.backend.dto.AttendanceSaveRequest;
import com.springlms.backend.dto.AttendanceSessionResponse;
import com.springlms.backend.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Teacher - Attendance", description = "Teacher endpoints for opening, reading, and saving attendance sessions.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/teacher/attendance")
@RequiredArgsConstructor
public class TeacherAttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/sessions/open")
    @Operation(summary = "Open attendance session", description = "Opens a new attendance session for a schedule entry.")
    public AttendanceSessionResponse openSession(Authentication authentication, @RequestBody AttendanceOpenRequest request) {
        return attendanceService.openSession(authentication.getName(), request);
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get attendance session", description = "Returns an existing attendance session and its records.")
    public AttendanceSessionResponse getSession(Authentication authentication, @PathVariable Long sessionId) {
        return attendanceService.getSession(authentication.getName(), sessionId);
    }

    @PutMapping("/sessions/{sessionId}")
    @Operation(summary = "Save attendance session", description = "Saves attendance records and optionally submits the session.")
    public AttendanceSessionResponse saveSession(
            Authentication authentication,
            @PathVariable Long sessionId,
            @RequestBody AttendanceSaveRequest request
    ) {
        return attendanceService.saveSession(authentication.getName(), sessionId, request);
    }
}
