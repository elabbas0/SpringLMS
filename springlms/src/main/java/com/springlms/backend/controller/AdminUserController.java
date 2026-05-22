package com.springlms.backend.controller;

import com.springlms.backend.dto.AdminDashboardResponse;
import com.springlms.backend.dto.CreateTeacherRequest;
import com.springlms.backend.dto.UserResponse;
import com.springlms.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard() {
        return userService.getAdminDashboard();
    }

    @PostMapping("/users/teachers")
    public UserResponse createTeacher(@RequestBody CreateTeacherRequest request) {
        return userService.createTeacher(request);
    }
}