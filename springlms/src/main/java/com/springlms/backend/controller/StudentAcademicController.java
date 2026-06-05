package com.springlms.backend.controller;

import com.springlms.backend.dto.ScheduleEntryResponse;
import com.springlms.backend.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Student - Academic", description = "Student endpoints for academic schedule access.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/student/academic")
@RequiredArgsConstructor
public class StudentAcademicController {

    private final ScheduleService scheduleService;

    @GetMapping("/schedule")
    @Operation(summary = "View student schedule", description = "Returns the authenticated student's schedule.")
    public List<ScheduleEntryResponse> listStudentSchedule(Authentication authentication) {
        return scheduleService.listStudentSchedule(authentication.getName());
    }
}
