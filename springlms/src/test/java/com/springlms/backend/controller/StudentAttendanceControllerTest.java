package com.springlms.backend.controller;

import com.springlms.backend.dto.StudentAttendanceEntryResponse;
import com.springlms.backend.dto.StudentAttendanceSummaryResponse;
import com.springlms.backend.model.attendance.AttendanceStatus;
import com.springlms.backend.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StudentAttendanceControllerTest {

    @Mock
    private AttendanceService attendanceService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new StudentAttendanceController(attendanceService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules()))
                .build();
    }

    @Test
    void getSummaryReturnsAttendanceSummary() throws Exception {
        when(attendanceService.getStudentSummary("student@example.com")).thenReturn(new StudentAttendanceSummaryResponse(87.5, 17.5, 20, 18, 2, 1.5, 70, 15));

        mockMvc.perform(get("/api/student/attendance/summary")
                        .principal(new UsernamePasswordAuthenticationToken("student@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_STUDENT")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendancePercentage").value(87.5))
                .andExpect(jsonPath("$.recordedSessions").value(18));
    }

    @Test
    void listEntriesReturnsAttendanceEntries() throws Exception {
        when(attendanceService.listStudentAttendanceEntries("student@example.com")).thenReturn(List.of(new StudentAttendanceEntryResponse(
                1L,
                2L,
                "2026-06-05",
                "FRIDAY",
                "Riyazi analiz",
                "LECTURE",
                "Teacher Name",
                AttendanceStatus.PRESENT
        )));

        mockMvc.perform(get("/api/student/attendance/entries")
                        .principal(new UsernamePasswordAuthenticationToken("student@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_STUDENT")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PRESENT"))
                .andExpect(jsonPath("$[0].subjectName").value("Riyazi analiz"));
    }
}
