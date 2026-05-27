package com.springlms.backend.controller;

import com.springlms.backend.dto.*;
import com.springlms.backend.service.CourseMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseMaterialService courseMaterialService;

    @GetMapping
    public List<CourseResponse> listCourses() {
        return courseMaterialService.listCourses();
    }

    @PostMapping
    public CourseResponse createCourse(@RequestBody CreateCourseRequest request) {
        return courseMaterialService.createCourse(request);
    }

    @GetMapping("/sections")
    public List<CourseSectionResponse> listSections() {
        return courseMaterialService.listAdminSections();
    }

    @PostMapping("/sections")
    public CourseSectionResponse createSection(Authentication authentication, @RequestBody CreateCourseSectionRequest request) {
        return courseMaterialService.createSection(authentication.getName(), request);
    }
}
