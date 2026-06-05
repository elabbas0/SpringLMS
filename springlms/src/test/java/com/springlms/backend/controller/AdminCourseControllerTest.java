package com.springlms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springlms.backend.dto.CourseAttachmentResponse;
import com.springlms.backend.dto.CourseResponse;
import com.springlms.backend.dto.CourseSectionGroupResponse;
import com.springlms.backend.dto.CourseSectionResponse;
import com.springlms.backend.dto.CourseSectionTeacherAssignmentResponse;
import com.springlms.backend.dto.CreateCourseRequest;
import com.springlms.backend.dto.CreateCourseSectionRequest;
import com.springlms.backend.dto.CreateCourseSectionTeacherAssignmentRequest;
import com.springlms.backend.model.course.TeacherAssignmentType;
import com.springlms.backend.service.CourseMaterialService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminCourseControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private CourseMaterialService courseMaterialService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminCourseController(courseMaterialService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void listCoursesReturnsCourses() throws Exception {
        when(courseMaterialService.listCourses()).thenReturn(List.of(new CourseResponse(1L, "WEB101", "Web Programming", "Introductory web programming materials.", 5, false)));

        mockMvc.perform(get("/api/admin/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("WEB101"))
                .andExpect(jsonPath("$[0].name").value("Web Programming"));
    }

    @Test
    void createCourseReturnsCreatedCourse() throws Exception {
        CreateCourseRequest request = new CreateCourseRequest("WEB101", "Web Programming", "Introductory web programming materials.", 5);
        when(courseMaterialService.createCourse(any(CreateCourseRequest.class))).thenReturn(new CourseResponse(1L, "WEB101", "Web Programming", "Introductory web programming materials.", 5, false));

        mockMvc.perform(post("/api/admin/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("WEB101"));
    }

    @Test
    void listSectionsReturnsSections() throws Exception {
        CourseSectionResponse response = new CourseSectionResponse(
                1L,
                1L,
                "WEB101",
                "Web Programming",
                "WP-6525A4",
                "2026 Spring",
                List.of(new CourseSectionGroupResponse(1L, "6525a4")),
                List.of(new CourseSectionTeacherAssignmentResponse(1L, 4L, "teacher@example.com", "Teacher Name", "LECTURE")),
                List.of(new CourseAttachmentResponse(1L, 1L, "WP-6525A4", "Web Programming", "Course outline", "DOCUMENT", "https://example.com/outline.pdf", "Syllabus", "Teacher Name", "2026-06-05T12:00:00")),
                20
        );
        when(courseMaterialService.listAdminSections()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/admin/courses/sections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sectionCode").value("WP-6525A4"))
                .andExpect(jsonPath("$[0].courseCode").value("WEB101"));
    }

    @Test
    void createSectionReturnsCreatedSection() throws Exception {
        CreateCourseSectionRequest request = new CreateCourseSectionRequest(
                1L,
                "WP-6525A4",
                List.of(1L, 2L),
                List.of(new CreateCourseSectionTeacherAssignmentRequest(4L, TeacherAssignmentType.LECTURE))
        );
        when(courseMaterialService.createSection(eq("admin@example.com"), any(CreateCourseSectionRequest.class))).thenReturn(
                new CourseSectionResponse(
                        1L,
                        1L,
                        "WEB101",
                        "Web Programming",
                        "WP-6525A4",
                        "2026 Spring",
                        List.of(),
                        List.of(),
                        List.of(),
                        20
                )
        );

        mockMvc.perform(post("/api/admin/courses/sections")
                        .principal(new UsernamePasswordAuthenticationToken("admin@example.com", "n/a", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionCode").value("WP-6525A4"));
    }
}
