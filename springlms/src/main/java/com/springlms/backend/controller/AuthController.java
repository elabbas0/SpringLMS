package com.springlms.backend.controller;

import com.springlms.backend.dto.LoginRequest;
import com.springlms.backend.dto.LoginResponse;
import com.springlms.backend.dto.StudentRegisterRequest;
import com.springlms.backend.dto.UserResponse;
import com.springlms.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Authentication endpoints for login and student registration.")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register/student")
    @Operation(summary = "Register a student account", description = "Creates a new student account in pending approval state.")
    public UserResponse registerStudent(@RequestBody StudentRegisterRequest request) {
        return userService.registerStudent(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT access token.")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
}
