package com.springlms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springlms.backend.dto.AssignStudentToGroupRequest;
import com.springlms.backend.dto.ScheduleEntryResponse;
import com.springlms.backend.dto.SpecializationResponse;
import com.springlms.backend.dto.StudentGroupResponse;
import com.springlms.backend.dto.TeacherStudentOptionResponse;
import com.springlms.backend.dto.UpdateScheduleEntryRequest;
import com.springlms.backend.model.user.State;
import com.springlms.backend.model.user.StudentFundingType;
import com.springlms.backend.service.AcademicStructureService;
import com.springlms.backend.service.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
class TeacherAcademicControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private AcademicStructureService academicStructureService;

    @Mock
    private ScheduleService scheduleService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TeacherAcademicController(academicStructureService, scheduleService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void listAssignedSpecializationsReturnsSpecializations() throws Exception {
        when(academicStructureService.listTeacherSpecializations("teacher@example.com")).thenReturn(List.of(
                new SpecializationResponse(1L, "Computer Science", 2L, "Engineering", 3L, "teacher@example.com", 30, false)
        ));

        mockMvc.perform(get("/api/teacher/academic/specializations")
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Computer Science"));
    }

    @Test
    void listStudentGroupsReturnsGroups() throws Exception {
        when(academicStructureService.listTeacherStudentGroups("teacher@example.com")).thenReturn(List.of(
                new StudentGroupResponse(1L, "6525a1", 2L, "Computer Science", 3L, "Engineering", 1, true, false, List.of(), List.of())
        ));

        mockMvc.perform(get("/api/teacher/academic/student-groups")
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("6525a1"));
    }

    @Test
    void listAvailableStudentsReturnsOptions() throws Exception {
        when(academicStructureService.listTeacherAssignableStudents("teacher@example.com")).thenReturn(List.of(
                new TeacherStudentOptionResponse(10L, "student@example.com", "Aysel", "Mammadova", State.ACTIVE, "6525a1", 460.5, 1, 2025, StudentFundingType.PERSONAL_CREDIT)
        ));

        mockMvc.perform(get("/api/teacher/academic/students")
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("student@example.com"));
    }

    @Test
    void assignStudentToGroupReturnsGroup() throws Exception {
        AssignStudentToGroupRequest request = new AssignStudentToGroupRequest(10L);
        when(academicStructureService.assignStudentToTeacherGroup(eq("teacher@example.com"), eq(1L), any(AssignStudentToGroupRequest.class))).thenReturn(
                new StudentGroupResponse(1L, "6525a1", 2L, "Computer Science", 3L, "Engineering", 1, true, false, List.of(), List.of())
        );

        mockMvc.perform(post("/api/teacher/academic/student-groups/1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("6525a1"));
    }

    @Test
    void listTeacherScheduleReturnsSchedule() throws Exception {
        when(scheduleService.listTeacherSchedule("teacher@example.com")).thenReturn(List.of(
                new ScheduleEntryResponse(1L, 1L, "6525a1", 1, 4L, "teacher@example.com", "Teacher Name", "Riyazi analiz", "LECTURE", "MONDAY", "2026-06-08", "09:00", "10:20")
        ));

        mockMvc.perform(get("/api/teacher/academic/schedule")
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teacherEmail").value("teacher@example.com"));
    }

    @Test
    void updateScheduleEntryReturnsUpdatedEntry() throws Exception {
        UpdateScheduleEntryRequest request = new UpdateScheduleEntryRequest("TUESDAY", "11:00");
        when(scheduleService.updateTeacherScheduleEntry(eq("teacher@example.com"), eq(1L), any(UpdateScheduleEntryRequest.class))).thenReturn(
                new ScheduleEntryResponse(1L, 1L, "6525a1", 1, 4L, "teacher@example.com", "Teacher Name", "Riyazi analiz", "LECTURE", "TUESDAY", "2026-06-09", "11:00", "12:20")
        );

        mockMvc.perform(patch("/api/teacher/academic/schedule/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayOfWeek").value("TUESDAY"))
                .andExpect(jsonPath("$.startTime").value("11:00"));
    }
}
