package com.springlms.backend.controller;

import com.springlms.backend.dto.ScheduleEntryResponse;
import com.springlms.backend.service.ScheduleService;
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
class StudentAcademicControllerTest {

    @Mock
    private ScheduleService scheduleService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new StudentAcademicController(scheduleService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules()))
                .build();
    }

    @Test
    void listStudentScheduleReturnsSchedule() throws Exception {
        when(scheduleService.listStudentSchedule("student@example.com")).thenReturn(List.of(new ScheduleEntryResponse(
                1L,
                1L,
                "6525a1",
                1,
                4L,
                "teacher@example.com",
                "Teacher Name",
                "Riyazi analiz",
                "LECTURE",
                "MONDAY",
                "2026-06-08",
                "09:00",
                "10:20"
        )));

        mockMvc.perform(get("/api/student/academic/schedule")
                        .principal(new UsernamePasswordAuthenticationToken("student@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_STUDENT")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teacherEmail").value("teacher@example.com"))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"));
    }
}
