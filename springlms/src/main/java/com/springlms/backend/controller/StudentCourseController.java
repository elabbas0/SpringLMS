package com.springlms.backend.controller;

import com.springlms.backend.dto.*;
import com.springlms.backend.service.CourseAssessmentService;
import com.springlms.backend.service.CourseMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Student - Materials", description = "Student endpoints for course materials, assignments, and submissions.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/student/materials")
@RequiredArgsConstructor
public class StudentCourseController {

    private final CourseMaterialService courseMaterialService;
    private final CourseAssessmentService courseAssessmentService;

    @GetMapping
    @Operation(summary = "List materials", description = "Returns course materials available to the authenticated student.")
    public List<StudentMaterialSectionResponse> listMaterials(Authentication authentication) {
        return courseMaterialService.listStudentMaterials(authentication.getName());
    }

    @GetMapping("/assignments")
    @Operation(summary = "List assignments", description = "Returns assignments available to the authenticated student.")
    public List<CourseAssignmentResponse> listAssignments(Authentication authentication) {
        return courseAssessmentService.listStudentAssignments(authentication.getName());
    }

    @GetMapping("/submissions")
    @Operation(summary = "List submissions", description = "Returns the authenticated student's assignment submissions.")
    public List<CourseAssignmentSubmissionResponse> listSubmissions(Authentication authentication) {
        return courseAssessmentService.listStudentSubmissions(authentication.getName());
    }

    @PostMapping("/assignments/{assignmentId}/submissions")
    @Operation(summary = "Submit assignment", description = "Submits text, a link, or a file for the selected assignment.")
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
    @Operation(summary = "Download attachment", description = "Downloads a course attachment when the student has access.")
    public ResponseEntity<Resource> downloadAttachment(Authentication authentication, @PathVariable Long attachmentId) {
        return courseAssessmentService.downloadAttachment(authentication.getName(), attachmentId);
    }
}
