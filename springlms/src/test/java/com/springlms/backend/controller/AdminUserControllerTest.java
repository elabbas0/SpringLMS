package com.springlms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springlms.backend.dto.AdminDashboardResponse;
import com.springlms.backend.dto.ChangeUserStateRequest;
import com.springlms.backend.dto.CreateFacultyRequest;
import com.springlms.backend.dto.CreateTeacherRequest;
import com.springlms.backend.dto.FacultyResponse;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminUserController(userService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void dashboardReturnsCounts() throws Exception {
        when(userService.getAdminDashboard()).thenReturn(new AdminDashboardResponse(10, 4, 3, 3, 1, 6, 1, 1, 0, 0, 1));

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalStudents").value(4))
                .andExpect(jsonPath("$.totalTeachers").value(3))
                .andExpect(jsonPath("$.totalAdmins").value(3));
    }

    @Test
    void searchUsersReturnsFilteredUsers() throws Exception {
        UserResponse user = new UserResponse(11L, "student@example.com", Role.STUDENT, State.ACTIVE, "Aysel", "Mammadova", 460.5, 1, 2025, StudentFundingType.PERSONAL_CREDIT);
        when(userService.searchUsers(eq("student"), eq(Role.STUDENT), eq(State.ACTIVE))).thenReturn(List.of(user));

        mockMvc.perform(get("/api/admin/users/search")
                        .param("query", "student")
                        .param("role", "STUDENT")
                        .param("state", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].email").value("student@example.com"))
                .andExpect(jsonPath("$[0].role").value("STUDENT"));
    }

    @Test
    void changeUserStateReturnsUpdatedUser() throws Exception {
        ChangeUserStateRequest request = new ChangeUserStateRequest(State.SUSPENDED);
        UserResponse response = new UserResponse(7L, "student@example.com", Role.STUDENT, State.SUSPENDED, "Aysel", "Mammadova", 460.5, 1, 2025, StudentFundingType.STATE_FUNDED);
        when(userService.changeUserState(eq(7L), any(ChangeUserStateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/admin/users/7/state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.state").value("SUSPENDED"));
    }

    @Test
    void createTeacherReturnsCreatedTeacher() throws Exception {
        CreateTeacherRequest request = new CreateTeacherRequest(
                "teacher@example.com",
                "Password1!",
                "TCH0001",
                "AZT0000001",
                "Murad",
                "Karimov",
                LocalDate.of(1990, 5, 10),
                "0501000001",
                "Baku",
                3L,
                20
        );
        UserResponse response = new UserResponse(21L, request.email(), Role.TEACHER, State.ACTIVE, request.firstName(), request.lastName(), null, null, null, null);
        when(userService.createTeacher(any(CreateTeacherRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/users/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(21))
                .andExpect(jsonPath("$.role").value("TEACHER"))
                .andExpect(jsonPath("$.email").value(request.email()));
    }

    @Test
    void listFacultiesReturnsAllFaculties() throws Exception {
        when(userService.listFaculties()).thenReturn(List.of(new FacultyResponse(1L, "Engineering", "ENG", false)));

        mockMvc.perform(get("/api/admin/faculties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Engineering"))
                .andExpect(jsonPath("$[0].code").value("ENG"));
    }

    @Test
    void createFacultyReturnsCreatedFaculty() throws Exception {
        CreateFacultyRequest request = new CreateFacultyRequest("Engineering", "ENG");
        when(userService.createFaculty(any(CreateFacultyRequest.class))).thenReturn(new FacultyResponse(1L, "Engineering", "ENG", false));

        mockMvc.perform(post("/api/admin/faculties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Engineering"))
                .andExpect(jsonPath("$.code").value("ENG"));
    }
}
