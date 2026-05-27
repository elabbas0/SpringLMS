package com.springlms.backend.controller;

import com.springlms.backend.dto.*;
import com.springlms.backend.service.CourseAssessmentService;
import com.springlms.backend.service.CourseMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/materials")
@RequiredArgsConstructor
public class TeacherCourseController {

    private final CourseMaterialService courseMaterialService;
    private final CourseAssessmentService courseAssessmentService;

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

    @PostMapping("/attachments/upload")
    public UploadCourseAttachmentResponse uploadAttachment(
            Authentication authentication,
            @RequestParam Long sectionId,
            @RequestParam String title,
            @RequestParam(required = false) String attachmentType,
            @RequestParam(required = false) String description,
            @RequestPart MultipartFile file
    ) {
        return courseAssessmentService.uploadAttachment(authentication.getName(), sectionId, title, attachmentType, description, file);
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(Authentication authentication, @PathVariable Long attachmentId) {
        return courseAssessmentService.downloadAttachment(authentication.getName(), attachmentId);
    }

    @GetMapping("/assignments")
    public List<CourseAssignmentResponse> listAssignments(Authentication authentication) {
        return courseAssessmentService.listTeacherAssignments(authentication.getName());
    }

    @PostMapping("/assignments")
    public CourseAssignmentResponse createAssignment(Authentication authentication, @RequestBody CreateCourseAssignmentRequest request) {
        return courseAssessmentService.createAssignment(authentication.getName(), request);
    }

    @GetMapping("/submissions")
    public List<CourseAssignmentSubmissionResponse> listSubmissions(Authentication authentication) {
        return courseAssessmentService.listTeacherSubmissions(authentication.getName());
    }

    @PutMapping("/submissions/{submissionId}/grade")
    public CourseAssignmentSubmissionResponse gradeSubmission(
            Authentication authentication,
            @PathVariable Long submissionId,
            @RequestBody GradeCourseAssignmentSubmissionRequest request
    ) {
        return courseAssessmentService.gradeSubmission(authentication.getName(), submissionId, request);
    }
}
