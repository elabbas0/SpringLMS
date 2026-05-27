package com.springlms.backend.controller;

import com.springlms.backend.dto.CourseAttachmentResponse;
import com.springlms.backend.dto.CourseSectionResponse;
import com.springlms.backend.dto.CreateCourseAttachmentRequest;
import com.springlms.backend.service.CourseMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/materials")
@RequiredArgsConstructor
public class TeacherCourseController {

    private final CourseMaterialService courseMaterialService;

    @GetMapping("/sections")
    public List<CourseSectionResponse> listSections(Authentication authentication) {
        return courseMaterialService.listTeacherSections(authentication.getName());
    }

    @GetMapping("/attachments")
    public List<CourseAttachmentResponse> listAttachments(Authentication authentication) {
        return courseMaterialService.listTeacherAttachments(authentication.getName());
    }

    @PostMapping("/attachments")
    public CourseAttachmentResponse createAttachment(Authentication authentication, @RequestBody CreateCourseAttachmentRequest request) {
        return courseMaterialService.createAttachment(authentication.getName(), request);
    }
}
