package com.springlms.backend.controller;

import com.springlms.backend.dto.AttendanceOpenRequest;
import com.springlms.backend.dto.AttendanceSaveRequest;
import com.springlms.backend.dto.AttendanceSessionResponse;
import com.springlms.backend.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher/attendance")
@RequiredArgsConstructor
public class TeacherAttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/sessions/open")
    public AttendanceSessionResponse openSession(Authentication authentication, @RequestBody AttendanceOpenRequest request) {
        return attendanceService.openSession(authentication.getName(), request);
    }

    @GetMapping("/sessions/{sessionId}")
    public AttendanceSessionResponse getSession(Authentication authentication, @PathVariable Long sessionId) {
        return attendanceService.getSession(authentication.getName(), sessionId);
    }

    @PutMapping("/sessions/{sessionId}")
    public AttendanceSessionResponse saveSession(
            Authentication authentication,
            @PathVariable Long sessionId,
            @RequestBody AttendanceSaveRequest request
    ) {
        return attendanceService.saveSession(authentication.getName(), sessionId, request);
    }
}
