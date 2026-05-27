package com.springlms.backend.controller;

import com.springlms.backend.dto.StudentMaterialSectionResponse;
import com.springlms.backend.service.CourseMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student/materials")
@RequiredArgsConstructor
public class StudentCourseController {

    private final CourseMaterialService courseMaterialService;

    @GetMapping
    public List<StudentMaterialSectionResponse> listMaterials(Authentication authentication) {
        return courseMaterialService.listStudentMaterials(authentication.getName());
    }
}
