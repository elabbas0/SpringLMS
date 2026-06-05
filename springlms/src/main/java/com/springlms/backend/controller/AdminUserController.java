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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin - Users", description = "Admin endpoints for dashboard, user management, and faculty management.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/dashboard")
    @Operation(summary = "View admin dashboard", description = "Returns high-level counts for users by role and state.")
    public AdminDashboardResponse dashboard() {
        return userService.getAdminDashboard();
    }

    @GetMapping("/users/search")
    @Operation(summary = "Search users", description = "Filters users by query, role, and state.")
    public List<UserResponse> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) State state
    ) {
        return userService.searchUsers(query, role, state);
    }

    @PatchMapping("/users/{userId}/state")
    @Operation(summary = "Change user state", description = "Updates a user account state such as ACTIVE or SUSPENDED.")
    public UserResponse changeUserState(
            @PathVariable Long userId,
            @RequestBody ChangeUserStateRequest request
    ) {
        return userService.changeUserState(userId, request);
    }

    @PostMapping("/users/teachers")
    @Operation(summary = "Create teacher", description = "Creates an active teacher account with profile data.")
    public UserResponse createTeacher(@RequestBody CreateTeacherRequest request) {
        return userService.createTeacher(request);
    }

    @GetMapping("/faculties")
    @Operation(summary = "List faculties", description = "Returns all faculties ordered by name.")
    public List<FacultyResponse> listFaculties() {
        return userService.listFaculties();
    }

    @PostMapping("/faculties")
    @Operation(summary = "Create faculty", description = "Creates a new faculty record.")
    public FacultyResponse createFaculty(@RequestBody CreateFacultyRequest request) {
        return userService.createFaculty(request);
    }
}
