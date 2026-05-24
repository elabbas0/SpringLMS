package com.springlms.backend.controller;

import com.springlms.backend.dto.AdminDashboardResponse;
import com.springlms.backend.dto.ChangeUserStateRequest;
import com.springlms.backend.dto.CreateFacultyRequest;
import com.springlms.backend.dto.CreateTeacherRequest;
import com.springlms.backend.dto.FacultyResponse;
import com.springlms.backend.dto.UserResponse;
import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.State;
import com.springlms.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard() {
        return userService.getAdminDashboard();
    }

    @GetMapping("/users/search")
    public List<UserResponse> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) State state
    ) {
        return userService.searchUsers(query, role, state);
    }

    @PatchMapping("/users/{userId}/state")
    public UserResponse changeUserState(
            @PathVariable Long userId,
            @RequestBody ChangeUserStateRequest request
    ) {
        return userService.changeUserState(userId, request);
    }

    @PostMapping("/users/teachers")
    public UserResponse createTeacher(@RequestBody CreateTeacherRequest request) {
        return userService.createTeacher(request);
    }

    @GetMapping("/faculties")
    public List<FacultyResponse> listFaculties() {
        return userService.listFaculties();
    }

    @PostMapping("/faculties")
    public FacultyResponse createFaculty(@RequestBody CreateFacultyRequest request) {
        return userService.createFaculty(request);
    }
}
