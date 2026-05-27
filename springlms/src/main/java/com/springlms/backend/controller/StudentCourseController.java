package com.springlms.backend.controller;

import com.springlms.backend.dto.*;
import com.springlms.backend.service.CourseAssessmentService;
import com.springlms.backend.service.CourseMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;

import java.util.List;

@RestController
@RequestMapping("/api/student/materials")
@RequiredArgsConstructor
public class StudentCourseController {

    private final CourseMaterialService courseMaterialService;
    private final CourseAssessmentService courseAssessmentService;

    @GetMapping
    public List<StudentMaterialSectionResponse> listMaterials(Authentication authentication) {
        return courseMaterialService.listStudentMaterials(authentication.getName());
    }

    @GetMapping("/assignments")
    public List<CourseAssignmentResponse> listAssignments(Authentication authentication) {
        return courseAssessmentService.listStudentAssignments(authentication.getName());
    }

    @GetMapping("/submissions")
    public List<CourseAssignmentSubmissionResponse> listSubmissions(Authentication authentication) {
        return courseAssessmentService.listStudentSubmissions(authentication.getName());
    }

    @PostMapping("/assignments/{assignmentId}/submissions")
    public CourseAssignmentSubmissionResponse submitAssignment(
            Authentication authentication,
            @PathVariable Long assignmentId,
            @RequestPart(required = false) MultipartFile file,
            @RequestParam(required = false) String textContent,
            @RequestParam(required = false) String linkUrl
    ) {
        SubmitCourseAssignmentRequest payload = new SubmitCourseAssignmentRequest(textContent, linkUrl);
        return courseAssessmentService.submitAssignment(authentication.getName(), assignmentId, payload, file);
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(Authentication authentication, @PathVariable Long attachmentId) {
        return courseAssessmentService.downloadAttachment(authentication.getName(), attachmentId);
    }
}
