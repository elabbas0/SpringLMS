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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Teacher - Materials", description = "Teacher endpoints for course materials, assignments, submissions, and grading.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/teacher/materials")
@RequiredArgsConstructor
public class TeacherCourseController {

    private final CourseMaterialService courseMaterialService;
    private final CourseAssessmentService courseAssessmentService;

    @GetMapping("/sections")
    @Operation(summary = "List sections", description = "Returns the sections assigned to the authenticated teacher.")
    public List<CourseSectionResponse> listSections(Authentication authentication) {
        return courseMaterialService.listTeacherSections(authentication.getName());
    }

    @GetMapping("/attachments")
    @Operation(summary = "List attachments", description = "Returns teacher-created attachments across assigned sections.")
    public List<CourseAttachmentResponse> listAttachments(Authentication authentication) {
        return courseMaterialService.listTeacherAttachments(authentication.getName());
    }

    @PostMapping("/attachments")
    @Operation(summary = "Create attachment", description = "Creates a link-based course attachment.")
    public CourseAttachmentResponse createAttachment(Authentication authentication, @RequestBody CreateCourseAttachmentRequest request) {
        return courseMaterialService.createAttachment(authentication.getName(), request);
    }

    @PostMapping("/attachments/upload")
    @Operation(summary = "Upload attachment", description = "Uploads a file attachment for a course section.")
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
    @Operation(summary = "Download attachment", description = "Downloads a teacher attachment file.")
    public ResponseEntity<Resource> downloadAttachment(Authentication authentication, @PathVariable Long attachmentId) {
        return courseAssessmentService.downloadAttachment(authentication.getName(), attachmentId);
    }

    @GetMapping("/assignments")
    @Operation(summary = "List assignments", description = "Returns assignments created by the authenticated teacher.")
    public List<CourseAssignmentResponse> listAssignments(Authentication authentication) {
        return courseAssessmentService.listTeacherAssignments(authentication.getName());
    }

    @PostMapping("/assignments")
    @Operation(summary = "Create assignment", description = "Creates a new assignment for one of the teacher's sections.")
    public CourseAssignmentResponse createAssignment(Authentication authentication, @RequestBody CreateCourseAssignmentRequest request) {
        return courseAssessmentService.createAssignment(authentication.getName(), request);
    }

    @GetMapping("/submissions")
    @Operation(summary = "List submissions", description = "Returns submissions for the teacher's assignments.")
    public List<CourseAssignmentSubmissionResponse> listSubmissions(Authentication authentication) {
        return courseAssessmentService.listTeacherSubmissions(authentication.getName());
    }

    @PutMapping("/submissions/{submissionId}/grade")
    @Operation(summary = "Grade submission", description = "Stores a score and feedback for a submission.")
    public CourseAssignmentSubmissionResponse gradeSubmission(
            Authentication authentication,
            @PathVariable Long submissionId,
            @RequestBody GradeCourseAssignmentSubmissionRequest request
    ) {
        return courseAssessmentService.gradeSubmission(authentication.getName(), submissionId, request);
    }
}
