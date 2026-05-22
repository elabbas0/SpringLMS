package com.springlms.backend.controller;

import com.springlms.backend.dto.LoginRequest;
import com.springlms.backend.dto.LoginResponse;
import com.springlms.backend.dto.StudentRegisterRequest;
import com.springlms.backend.dto.UserResponse;
import com.springlms.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register/student")
    public UserResponse registerStudent(@RequestBody StudentRegisterRequest request) {
        return userService.registerStudent(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
}