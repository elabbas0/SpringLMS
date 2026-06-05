package com.springlms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springlms.backend.dto.CourseAttachmentResponse;
import com.springlms.backend.dto.CourseAssignmentResponse;
import com.springlms.backend.dto.CourseAssignmentSubmissionResponse;
import com.springlms.backend.dto.StudentMaterialSectionResponse;
import com.springlms.backend.dto.SubmitCourseAssignmentRequest;
import com.springlms.backend.dto.UploadCourseAttachmentResponse;
import com.springlms.backend.model.attendance.AttendanceStatus;
import com.springlms.backend.service.CourseAssessmentService;
import com.springlms.backend.service.CourseMaterialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StudentCourseControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private CourseMaterialService courseMaterialService;

    @Mock
    private CourseAssessmentService courseAssessmentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new StudentCourseController(courseMaterialService, courseAssessmentService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper), new ResourceHttpMessageConverter())
                .build();
    }

    @Test
    void listMaterialsReturnsSections() throws Exception {
        when(courseMaterialService.listStudentMaterials("student@example.com")).thenReturn(List.of(
                new StudentMaterialSectionResponse(
                        1L,
                        "WEB101",
                        "Web Programming",
                        "WP-6525A4",
                        List.of(new CourseAttachmentResponse(1L, 1L, "WP-6525A4", "Web Programming", "Course outline", "DOCUMENT", "https://example.com/outline.pdf", "Syllabus", "Teacher Name", "2026-06-05T12:00:00"))
                )
        ));

        mockMvc.perform(get("/api/student/materials")
                        .principal(new UsernamePasswordAuthenticationToken("student@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_STUDENT")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("WEB101"))
                .andExpect(jsonPath("$[0].attachments[0].title").value("Course outline"));
    }

    @Test
    void listAssignmentsReturnsAssignments() throws Exception {
        when(courseAssessmentService.listStudentAssignments("student@example.com")).thenReturn(List.of(
                new CourseAssignmentResponse(1L, 1L, "WP-6525A4", "Web Programming", "Assignment 1", "Build a page", "2026-06-10", 100, "Teacher Name", "2026-06-05T12:00:00")
        ));

        mockMvc.perform(get("/api/student/materials/assignments")
                        .principal(new UsernamePasswordAuthenticationToken("student@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_STUDENT")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Assignment 1"))
                .andExpect(jsonPath("$[0].maxScore").value(100));
    }

    @Test
    void listSubmissionsReturnsSubmissions() throws Exception {
        when(courseAssessmentService.listStudentSubmissions("student@example.com")).thenReturn(List.of(
                new CourseAssignmentSubmissionResponse(1L, 2L, "Assignment 1", 10L, "Student Name", "Text", "submission.txt", "text/plain", "https://example.com", 95, "Good job", "2026-06-05T12:00:00", "2026-06-06T12:00:00", "Teacher Name")
        ));

        mockMvc.perform(get("/api/student/materials/submissions")
                        .principal(new UsernamePasswordAuthenticationToken("student@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_STUDENT")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(95))
                .andExpect(jsonPath("$[0].gradedBy").value("Teacher Name"));
    }

    @Test
    void submitAssignmentReturnsSubmission() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "submission.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8));
        CourseAssignmentSubmissionResponse response = new CourseAssignmentSubmissionResponse(1L, 2L, "Assignment 1", 10L, "Student Name", "Text", "submission.txt", "text/plain", "https://example.com", null, null, "2026-06-05T12:00:00", null, null);

        when(courseAssessmentService.submitAssignment(eq("student@example.com"), eq(2L), any(SubmitCourseAssignmentRequest.class), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/student/materials/assignments/2/submissions")
                        .file(file)
                        .param("textContent", "Text")
                        .param("linkUrl", "https://example.com")
                        .principal(new UsernamePasswordAuthenticationToken("student@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_STUDENT")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentId").value(2))
                .andExpect(jsonPath("$.fileName").value("submission.txt"));
    }

    @Test
    void downloadAttachmentReturnsResource() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("file-content".getBytes(StandardCharsets.UTF_8));
        when(courseAssessmentService.downloadAttachment("student@example.com", 9L)).thenReturn(
                ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=outline.pdf")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(resource)
        );

        mockMvc.perform(get("/api/student/materials/attachments/9/download")
                        .principal(new UsernamePasswordAuthenticationToken("student@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_STUDENT")))))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=outline.pdf"));
    }
}
