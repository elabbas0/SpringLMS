package com.springlms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springlms.backend.dto.ChangeStudentGroupApprovalRequest;
import com.springlms.backend.dto.CreateSpecializationRequest;
import com.springlms.backend.dto.CreateStudentGroupRequest;
import com.springlms.backend.dto.CreateStudentGroupTeacherAssignmentRequest;
import com.springlms.backend.dto.GlobalScheduleConfigRequest;
import com.springlms.backend.dto.GlobalScheduleConfigResponse;
import com.springlms.backend.dto.ScheduleEntryResponse;
import com.springlms.backend.dto.SpecializationResponse;
import com.springlms.backend.dto.StudentGroupResponse;
import com.springlms.backend.dto.StudentGroupTeacherAssignmentResponse;
import com.springlms.backend.service.AcademicStructureService;
import com.springlms.backend.service.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminAcademicControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private AcademicStructureService academicStructureService;

    @Mock
    private ScheduleService scheduleService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminAcademicController(academicStructureService, scheduleService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void listSpecializationsReturnsAllSpecializations() throws Exception {
        when(academicStructureService.listSpecializations()).thenReturn(List.of(
                new SpecializationResponse(1L, "Computer Science", 2L, "Engineering", 3L, "teacher@example.com", 30, false)
        ));

        mockMvc.perform(get("/api/admin/academic/specializations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Computer Science"));
    }

    @Test
    void createSpecializationReturnsCreatedSpecialization() throws Exception {
        CreateSpecializationRequest request = new CreateSpecializationRequest("Computer Science", 2L, 3L, 30);
        when(academicStructureService.createSpecialization(any(CreateSpecializationRequest.class))).thenReturn(
                new SpecializationResponse(1L, "Computer Science", 2L, "Engineering", 3L, "teacher@example.com", 30, false)
        );

        mockMvc.perform(post("/api/admin/academic/specializations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.facultyId").value(2));
    }

    @Test
    void listStudentGroupsReturnsAllGroups() throws Exception {
        StudentGroupResponse response = new StudentGroupResponse(
                1L,
                "6525a1",
                2L,
                "Computer Science",
                3L,
                "Engineering",
                1,
                true,
                false,
                List.of(new StudentGroupTeacherAssignmentResponse(1L, 4L, "teacher@example.com", "Teacher Name", "Riyazi analiz", 7, 1, 1, 0)),
                List.of()
        );
        when(academicStructureService.listStudentGroups()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/admin/academic/student-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("6525a1"))
                .andExpect(jsonPath("$[0].approved").value(true));
    }

    @Test
    void createStudentGroupReturnsCreatedGroup() throws Exception {
        CreateStudentGroupRequest request = new CreateStudentGroupRequest(
                "6525a1",
                2L,
                1,
                List.of(new CreateStudentGroupTeacherAssignmentRequest(4L, "Riyazi analiz", 7, 1, 1, 0))
        );
        StudentGroupResponse response = new StudentGroupResponse(
                1L,
                "6525a1",
                2L,
                "Computer Science",
                3L,
                "Engineering",
                1,
                true,
                false,
                List.of(),
                List.of()
        );
        when(academicStructureService.createStudentGroup(any(CreateStudentGroupRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/academic/student-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("6525a1"));
    }

    @Test
    void changeStudentGroupApprovalReturnsUpdatedGroup() throws Exception {
        ChangeStudentGroupApprovalRequest request = new ChangeStudentGroupApprovalRequest(false);
        when(academicStructureService.changeStudentGroupApproval(eq(1L), any(ChangeStudentGroupApprovalRequest.class))).thenReturn(
                new StudentGroupResponse(1L, "6525a1", 2L, "Computer Science", 3L, "Engineering", 1, false, false, List.of(), List.of())
        );

        mockMvc.perform(patch("/api/admin/academic/student-groups/1/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").value(false));
    }

    @Test
    void getGlobalConfigReturnsConfig() throws Exception {
        when(scheduleService.getGlobalConfig()).thenReturn(new GlobalScheduleConfigResponse(
                1.0,
                0.5,
                2,
                80,
                15,
                70,
                "09:00",
                "13:20",
                "09:00",
                "15:00",
                "09:00",
                "16:30",
                "09:00",
                "18:00"
        ));

        mockMvc.perform(get("/api/admin/academic/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lectureLengthMinutes").value(80))
                .andExpect(jsonPath("$.year4EndTime").value("18:00"));
    }

    @Test
    void updateGlobalConfigReturnsUpdatedConfig() throws Exception {
        GlobalScheduleConfigRequest request = new GlobalScheduleConfigRequest(
                1.0,
                0.5,
                2,
                80,
                15,
                70,
                "09:00",
                "13:20",
                "09:00",
                "15:00",
                "09:00",
                "16:30",
                "09:00",
                "18:00"
        );
        when(scheduleService.updateGlobalConfig(any(GlobalScheduleConfigRequest.class))).thenReturn(
                new GlobalScheduleConfigResponse(
                        1.0,
                        0.5,
                        2,
                        80,
                        15,
                        70,
                        "09:00",
                        "13:20",
                        "09:00",
                        "15:00",
                        "09:00",
                        "16:30",
                        "09:00",
                        "18:00"
                )
        );

        mockMvc.perform(put("/api/admin/academic/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minimumAttendancePercentForFinalExam").value(70));
    }

    @Test
    void listStudentGroupScheduleReturnsSchedule() throws Exception {
        when(scheduleService.listStudentGroupSchedule(1L)).thenReturn(List.of(new ScheduleEntryResponse(
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

        mockMvc.perform(get("/api/admin/academic/student-groups/1/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$[0].subjectName").value("Riyazi analiz"));
    }

    @Test
    void generateStudentGroupScheduleReturnsGeneratedSchedule() throws Exception {
        when(scheduleService.generateStudentGroupSchedule(1L)).thenReturn(List.of(new ScheduleEntryResponse(
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

        mockMvc.perform(post("/api/admin/academic/student-groups/1/schedule/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentGroupName").value("6525a1"));
    }
}
