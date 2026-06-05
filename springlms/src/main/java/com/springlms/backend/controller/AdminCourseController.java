package com.springlms.backend.controller;

import com.springlms.backend.dto.*;
import com.springlms.backend.service.CourseMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin - Courses", description = "Admin endpoints for courses and course sections.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseMaterialService courseMaterialService;

    @GetMapping
    @Operation(summary = "List courses", description = "Returns all course records.")
    public List<CourseResponse> listCourses() {
        return courseMaterialService.listCourses();
    }

    @PostMapping
    @Operation(summary = "Create course", description = "Creates a new course record.")
    public CourseResponse createCourse(@RequestBody CreateCourseRequest request) {
        return courseMaterialService.createCourse(request);
    }

    @GetMapping("/sections")
    @Operation(summary = "List sections", description = "Returns all course sections visible to admins.")
    public List<CourseSectionResponse> listSections() {
        return courseMaterialService.listAdminSections();
    }

    @PostMapping("/sections")
    @Operation(summary = "Create section", description = "Creates a section, assigns groups, and enrolls students.")
    public CourseSectionResponse createSection(Authentication authentication, @RequestBody CreateCourseSectionRequest request) {
        return courseMaterialService.createSection(authentication.getName(), request);
    }
}
