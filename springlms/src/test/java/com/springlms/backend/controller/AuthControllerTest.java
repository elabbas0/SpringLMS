package com.springlms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springlms.backend.dto.LoginRequest;
import com.springlms.backend.dto.LoginResponse;
import com.springlms.backend.dto.StudentRegisterRequest;
import com.springlms.backend.dto.UserResponse;
import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.State;
import com.springlms.backend.model.user.StudentFundingType;
import com.springlms.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(userService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void registerStudentReturnsCreatedUser() throws Exception {
        StudentRegisterRequest request = new StudentRegisterRequest(
                "student@example.com",
                "Password1!",
                "ABC1234",
                "AZS123456",
                "Aysel",
                "Mammadova",
                LocalDate.of(2006, 1, 10),
                "0502000001",
                "Baku",
                460.5,
                1,
                2025,
                StudentFundingType.PERSONAL_CREDIT,
                "Parent",
                "0503000001",
                "Parent"
        );

        UserResponse response = new UserResponse(
                12L,
                request.email(),
                Role.STUDENT,
                State.PENDING_APPROVAL,
                request.firstName(),
                request.lastName(),
                request.entranceScore(),
                request.studyYear(),
                request.admissionYear(),
                request.fundingType()
        );

        when(userService.registerStudent(any(StudentRegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.email").value(request.email()))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.state").value("PENDING_APPROVAL"));
    }

    @Test
    void loginReturnsToken() throws Exception {
        LoginRequest request = new LoginRequest("teacher@example.com", "Password1!");
        LoginResponse response = new LoginResponse(
                5L,
                request.email(),
                Role.TEACHER,
                State.ACTIVE,
                "token-value",
                "Bearer"
        );

        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.email").value(request.email()))
                .andExpect(jsonPath("$.token").value("token-value"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginMapsUnauthorizedException() throws Exception {
        LoginRequest request = new LoginRequest("teacher@example.com", "bad-password");
        when(userService.login(any(LoginRequest.class))).thenThrow(new com.springlms.backend.exception.UnauthorizedException("Invalid email or password."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void registerStudentMapsIllegalArgumentException() throws Exception {
        StudentRegisterRequest request = new StudentRegisterRequest(
                "student@example.com",
                "Password1!",
                "ABC1234",
                "AZS123456",
                "Aysel",
                "Mammadova",
                LocalDate.of(2006, 1, 10),
                "0502000001",
                "Baku",
                460.5,
                1,
                2025,
                StudentFundingType.PERSONAL_CREDIT,
                "Parent",
                "0503000001",
                "Parent"
        );

        when(userService.registerStudent(any(StudentRegisterRequest.class))).thenThrow(new IllegalArgumentException("Email is already registered."));

        mockMvc.perform(post("/api/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email is already registered."));
    }
}
