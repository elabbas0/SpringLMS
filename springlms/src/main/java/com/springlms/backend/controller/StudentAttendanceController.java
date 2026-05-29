package com.springlms.backend.controller;

import com.springlms.backend.dto.StudentAttendanceSummaryResponse;
import com.springlms.backend.dto.StudentAttendanceEntryResponse;
import com.springlms.backend.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/attendance")
@RequiredArgsConstructor
public class StudentAttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/summary")
    public StudentAttendanceSummaryResponse getSummary(Authentication authentication) {
        return attendanceService.getStudentSummary(authentication.getName());
    }

    @GetMapping("/entries")
    public java.util.List<StudentAttendanceEntryResponse> listEntries(Authentication authentication) {
        return attendanceService.listStudentAttendanceEntries(authentication.getName());
    }
}
