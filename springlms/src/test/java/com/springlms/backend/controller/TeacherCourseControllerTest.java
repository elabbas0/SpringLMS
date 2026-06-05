package com.springlms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springlms.backend.dto.CourseAssignmentResponse;
import com.springlms.backend.dto.CourseAssignmentSubmissionResponse;
import com.springlms.backend.dto.CourseAttachmentResponse;
import com.springlms.backend.dto.CourseSectionResponse;
import com.springlms.backend.dto.CreateCourseAssignmentRequest;
import com.springlms.backend.dto.CreateCourseAttachmentRequest;
import com.springlms.backend.dto.GradeCourseAssignmentSubmissionRequest;
import com.springlms.backend.dto.UploadCourseAttachmentResponse;
import com.springlms.backend.model.attendance.AttendanceStatus;
import com.springlms.backend.model.course.AttachmentType;
import com.springlms.backend.model.course.TeacherAssignmentType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TeacherCourseControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private CourseMaterialService courseMaterialService;

    @Mock
    private CourseAssessmentService courseAssessmentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TeacherCourseController(courseMaterialService, courseAssessmentService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper), new ResourceHttpMessageConverter())
                .build();
    }

    @Test
    void listSectionsReturnsSections() throws Exception {
        when(courseMaterialService.listTeacherSections("teacher@example.com")).thenReturn(List.of(
                new CourseSectionResponse(1L, 1L, "WEB101", "Web Programming", "WP-6525A4", "2026 Spring", List.of(), List.of(), List.of(), 20)
        ));

        mockMvc.perform(get("/api/teacher/materials/sections")
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sectionCode").value("WP-6525A4"));
    }

    @Test
    void listAttachmentsReturnsAttachments() throws Exception {
        when(courseMaterialService.listTeacherAttachments("teacher@example.com")).thenReturn(List.of(
                new CourseAttachmentResponse(1L, 1L, "WP-6525A4", "Web Programming", "Course outline", "DOCUMENT", "https://example.com/outline.pdf", "Syllabus", "Teacher Name", "2026-06-05T12:00:00")
        ));

        mockMvc.perform(get("/api/teacher/materials/attachments")
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Course outline"));
    }

    @Test
    void createAttachmentReturnsAttachment() throws Exception {
        CreateCourseAttachmentRequest request = new CreateCourseAttachmentRequest(1L, "Course outline", AttachmentType.DOCUMENT, "https://example.com/outline.pdf", "Syllabus");
        when(courseMaterialService.createAttachment(eq("teacher@example.com"), any(CreateCourseAttachmentRequest.class))).thenReturn(
                new CourseAttachmentResponse(1L, 1L, "WP-6525A4", "Web Programming", "Course outline", "DOCUMENT", "https://example.com/outline.pdf", "Syllabus", "Teacher Name", "2026-06-05T12:00:00")
        );

        mockMvc.perform(post("/api/teacher/materials/attachments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Course outline"));
    }

    @Test
    void uploadAttachmentReturnsUploadedAttachment() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "outline.pdf", "application/pdf", "file-content".getBytes(StandardCharsets.UTF_8));
        when(courseAssessmentService.uploadAttachment(eq("teacher@example.com"), eq(1L), eq("Course outline"), eq("DOCUMENT"), eq("Syllabus"), any())).thenReturn(
                new UploadCourseAttachmentResponse(1L, "Course outline", "DOCUMENT", "outline.pdf", "https://example.com/outline.pdf")
        );

        mockMvc.perform(multipart("/api/teacher/materials/attachments/upload")
                        .file(file)
                        .param("sectionId", "1")
                        .param("title", "Course outline")
                        .param("attachmentType", "DOCUMENT")
                        .param("description", "Syllabus")
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("outline.pdf"));
    }

    @Test
    void downloadAttachmentReturnsResource() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("file-content".getBytes(StandardCharsets.UTF_8));
        when(courseAssessmentService.downloadAttachment("teacher@example.com", 1L)).thenReturn(
                ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=outline.pdf")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(resource)
        );

        mockMvc.perform(get("/api/teacher/materials/attachments/1/download")
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=outline.pdf"));
    }

    @Test
    void listAssignmentsReturnsAssignments() throws Exception {
        when(courseAssessmentService.listTeacherAssignments("teacher@example.com")).thenReturn(List.of(
                new CourseAssignmentResponse(1L, 1L, "WP-6525A4", "Web Programming", "Assignment 1", "Build a page", "2026-06-10", 100, "Teacher Name", "2026-06-05T12:00:00")
        ));

        mockMvc.perform(get("/api/teacher/materials/assignments")
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Assignment 1"));
    }

    @Test
    void createAssignmentReturnsAssignment() throws Exception {
        CreateCourseAssignmentRequest request = new CreateCourseAssignmentRequest(1L, "Assignment 1", "Build a page", "2026-06-10", 100);
        when(courseAssessmentService.createAssignment(eq("teacher@example.com"), any(CreateCourseAssignmentRequest.class))).thenReturn(
                new CourseAssignmentResponse(1L, 1L, "WP-6525A4", "Web Programming", "Assignment 1", "Build a page", "2026-06-10", 100, "Teacher Name", "2026-06-05T12:00:00")
        );

        mockMvc.perform(post("/api/teacher/materials/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxScore").value(100));
    }

    @Test
    void listSubmissionsReturnsSubmissions() throws Exception {
        when(courseAssessmentService.listTeacherSubmissions("teacher@example.com")).thenReturn(List.of(
                new CourseAssignmentSubmissionResponse(1L, 2L, "Assignment 1", 10L, "Student Name", "Text", "submission.txt", "text/plain", "https://example.com", 95, "Good job", "2026-06-05T12:00:00", "2026-06-06T12:00:00", "Teacher Name")
        ));

        mockMvc.perform(get("/api/teacher/materials/submissions")
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(95));
    }

    @Test
    void gradeSubmissionReturnsSubmission() throws Exception {
        GradeCourseAssignmentSubmissionRequest request = new GradeCourseAssignmentSubmissionRequest(95, "Good job");
        when(courseAssessmentService.gradeSubmission(eq("teacher@example.com"), eq(1L), any(GradeCourseAssignmentSubmissionRequest.class))).thenReturn(
                new CourseAssignmentSubmissionResponse(1L, 2L, "Assignment 1", 10L, "Student Name", "Text", "submission.txt", "text/plain", "https://example.com", 95, "Good job", "2026-06-05T12:00:00", "2026-06-06T12:00:00", "Teacher Name")
        );

        mockMvc.perform(put("/api/teacher/materials/submissions/1/grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(new UsernamePasswordAuthenticationToken("teacher@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teacherFeedback").value("Good job"));
    }
}
