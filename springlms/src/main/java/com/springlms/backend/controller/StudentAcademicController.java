package com.springlms.backend.controller;

import com.springlms.backend.dto.ScheduleEntryResponse;
import com.springlms.backend.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student/academic")
@RequiredArgsConstructor
public class StudentAcademicController {

    private final ScheduleService scheduleService;

    @GetMapping("/schedule")
    public List<ScheduleEntryResponse> listStudentSchedule(Authentication authentication) {
        return scheduleService.listStudentSchedule(authentication.getName());
    }
}
