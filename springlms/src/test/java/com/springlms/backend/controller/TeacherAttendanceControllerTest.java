package com.springlms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springlms.backend.dto.AttendanceOpenRequest;
import com.springlms.backend.dto.AttendanceRecordResponse;
import com.springlms.backend.dto.AttendanceRecordUpdateRequest;
import com.springlms.backend.dto.AttendanceSaveRequest;
import com.springlms.backend.dto.AttendanceSessionResponse;
import com.springlms.backend.model.attendance.AttendanceStatus;
import com.springlms.backend.service.AttendanceService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TeacherAttendanceControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private AttendanceService attendanceService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TeacherAttendanceController(attendanceService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void openSessionReturnsSession() throws Exception {
        AttendanceOpenRequest request = new AttendanceOpenRequest(1L, "2026-06-05");
        when(attendanceService.openSession(eq("teacher@example.com"), any(AttendanceOpenRequest.class))).thenReturn(new AttendanceSessionResponse(
                1L,
                1L,
                1L,
                "6525a1",
                "Riyazi analiz",
                "LECTURE",
                "MONDAY",
                "09:00",
                "10:20",
                "2026-06-05",
                false,
                false,
                null,
                List.of()
        ));

        mockMvc.perform(post("/api/teacher/attendance/sessions/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleEntryId").value(1));
    }

    @Test
    void getSessionReturnsSession() throws Exception {
        when(attendanceService.getSession("teacher@example.com", 1L)).thenReturn(new AttendanceSessionResponse(
                1L,
                1L,
                1L,
                "6525a1",
                "Riyazi analiz",
                "LECTURE",
                "MONDAY",
                "09:00",
                "10:20",
                "2026-06-05",
                false,
                false,
                null,
                List.of(new AttendanceRecordResponse(10L, "student@example.com", "Student", "Name", AttendanceStatus.PRESENT))
        ));

        mockMvc.perform(get("/api/teacher/attendance/sessions/1")
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records[0].status").value("PRESENT"));
    }

    @Test
    void saveSessionReturnsUpdatedSession() throws Exception {
        AttendanceSaveRequest request = new AttendanceSaveRequest(
                List.of(new AttendanceRecordUpdateRequest(10L, AttendanceStatus.PRESENT)),
                true
        );
        when(attendanceService.saveSession(eq("teacher@example.com"), eq(1L), any(AttendanceSaveRequest.class))).thenReturn(new AttendanceSessionResponse(
                1L,
                1L,
                1L,
                "6525a1",
                "Riyazi analiz",
                "LECTURE",
                "MONDAY",
                "09:00",
                "10:20",
                "2026-06-05",
                true,
                true,
                "2026-06-05T12:00:00",
                List.of(new AttendanceRecordResponse(10L, "student@example.com", "Student", "Name", AttendanceStatus.PRESENT))
        ));

        mockMvc.perform(put("/api/teacher/attendance/sessions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submitted").value(true))
                .andExpect(jsonPath("$.locked").value(true));
    }
}
