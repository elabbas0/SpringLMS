package com.springlms.backend.service;

import com.springlms.backend.dto.*;
import com.springlms.backend.exception.BadRequestException;
import com.springlms.backend.exception.ForbiddenException;
import com.springlms.backend.exception.NotFoundException;
import com.springlms.backend.model.course.*;
import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.StudentProfile;
import com.springlms.backend.model.user.TeacherProfile;
import com.springlms.backend.model.user.User;
import com.springlms.backend.repository.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseAssessmentService {

    private final CourseSectionRepository courseSectionRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final CourseAssignmentSubmissionRepository courseAssignmentSubmissionRepository;
    private final CourseAttachmentRepository courseAttachmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public List<CourseAssignmentResponse> listTeacherAssignments(String teacherEmail) {
        return courseAssignmentRepository.findBySectionTeacherAssignmentsTeacherUserEmailOrderByDueDateAscCreatedAtDesc(teacherEmail)
                .stream()
                .map(this::toAssignmentResponse)
                .toList();
    }

    public List<CourseAssignmentResponse> listStudentAssignments(String studentEmail) {
        return courseAssignmentRepository.findBySectionEnrollmentsStudentUserEmailOrderByDueDateAscCreatedAtDesc(studentEmail)
                .stream()
                .map(this::toAssignmentResponse)
                .toList();
    }

    public List<CourseAssignmentSubmissionResponse> listTeacherSubmissions(String teacherEmail) {
        return courseAssignmentSubmissionRepository.findByAssignmentSectionTeacherAssignmentsTeacherUserEmailOrderBySubmittedAtDesc(teacherEmail)
                .stream()
                .map(this::toSubmissionResponse)
                .toList();
    }

    public List<CourseAssignmentSubmissionResponse> listStudentSubmissions(String studentEmail) {
        User student = findUser(studentEmail, Role.STUDENT);
        return courseAssignmentSubmissionRepository.findByStudentIdOrderBySubmittedAtDesc(student.getStudentProfile().getId())
                .stream()
                .map(this::toSubmissionResponse)
                .toList();
    }

    @Transactional
    public CourseAssignmentResponse createAssignment(String teacherEmail, CreateCourseAssignmentRequest request) {
        TeacherProfile teacher = findTeacherProfile(teacherEmail);
        CourseSection section = courseSectionRepository.findById(request.sectionId())
                .orElseThrow(() -> new NotFoundException("Course section not found."));

        boolean ownsSection = section.getTeacherAssignments()
                .stream()
                .anyMatch(assignment -> assignment.getTeacher().getId().equals(teacher.getId()));
        if (!ownsSection) {
            throw new ForbiddenException("You can only create assignments for your own sections.");
        }

        CourseAssignment assignment = courseAssignmentRepository.save(CourseAssignment.builder()
                .section(section)
                .teacher(teacher)
                .title(requireText(request.title(), "Assignment title"))
                .description(trimToNull(request.description()))
                .dueDate(parseDate(request.dueDate()))
                .maxScore(requirePositive(request.maxScore(), "Max score"))
                .archived(false)
                .build());

        return toAssignmentResponse(assignment);
    }

    @Transactional
    public CourseAssignmentSubmissionResponse submitAssignment(
            String studentEmail,
            Long assignmentId,
            SubmitCourseAssignmentRequest request,
            MultipartFile file
    ) {
        User studentUser = findUser(studentEmail, Role.STUDENT);
        StudentProfile student = studentUser.getStudentProfile();
        CourseAssignment assignment = courseAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found."));

        boolean enrolled = courseSectionRepository.findDistinctByEnrollmentsStudentUserEmailOrderBySectionCodeAsc(studentEmail)
                .stream()
                .anyMatch(section -> section.getId().equals(assignment.getSection().getId()));
        if (!enrolled) {
            throw new ForbiddenException("You are not enrolled in this section.");
        }

        CourseAssignmentSubmission submission = courseAssignmentSubmissionRepository.findByAssignmentIdAndStudentId(assignmentId, student.getId())
                .orElseGet(() -> CourseAssignmentSubmission.builder()
                        .assignment(assignment)
                        .student(student)
                        .build());

        String textContent = trimToNull(request.textContent());
        String linkUrl = trimToNull(request.linkUrl());
        if ((textContent == null || textContent.isBlank()) && (linkUrl == null || linkUrl.isBlank()) && (file == null || file.isEmpty())) {
            throw new BadRequestException("Submission content is required.");
        }

        submission.setTextContent(textContent);
        submission.setLinkUrl(linkUrl);
        submission.setSubmittedAt(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            // delete previous file if re-submitting
            if (submission.getStoragePath() != null) {
                fileStorageService.delete(submission.getStoragePath());
            }
            String key = fileStorageService.store(file);
            submission.setFileName(file.getOriginalFilename());
            submission.setFileContentType(file.getContentType());
            submission.setStoragePath(key);
        }

        return toSubmissionResponse(courseAssignmentSubmissionRepository.save(submission));
    }

    @Transactional
    public CourseAssignmentSubmissionResponse gradeSubmission(String teacherEmail, Long submissionId, GradeCourseAssignmentSubmissionRequest request) {
        TeacherProfile teacher = findTeacherProfile(teacherEmail);
        CourseAssignmentSubmission submission = courseAssignmentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found."));

        if (!submission.getAssignment().getSection().getTeacherAssignments()
                .stream()
                .anyMatch(assignment -> assignment.getTeacher().getId().equals(teacher.getId()))) {
            throw new ForbiddenException("You can only grade submissions for your own sections.");
        }

        if (request.score() == null || request.score() < 0 || request.score() > submission.getAssignment().getMaxScore()) {
            throw new BadRequestException("Score must be between 0 and the assignment maximum.");
        }

        submission.setScore(request.score());
        submission.setTeacherFeedback(trimToNull(request.teacherFeedback()));
        submission.setGradedBy(teacher);
        submission.setGradedAt(LocalDateTime.now());

        return toSubmissionResponse(courseAssignmentSubmissionRepository.save(submission));
    }

    @Transactional
    public UploadCourseAttachmentResponse uploadAttachment(
            String teacherEmail,
            Long sectionId,
            String title,
            String attachmentTypeValue,
            String description,
            MultipartFile file
    ) {
        TeacherProfile teacher = findTeacherProfile(teacherEmail);
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required.");
        }

        CourseSection section = resolveTeacherSection(teacherEmail, sectionId);
        AttachmentType attachmentType = parseAttachmentType(attachmentTypeValue, file.getContentType());

        String key = fileStorageService.store(file);

        CourseAttachment attachment = courseAttachmentRepository.save(CourseAttachment.builder()
                .section(section)
                .teacher(teacher)
                .attachmentType(attachmentType)
                .title(requireText(title, "Title"))
                .description(trimToNull(description))
                .resourceUrl("file://" + file.getOriginalFilename())
                .fileName(file.getOriginalFilename())
                .fileContentType(file.getContentType())
                .storagePath(key)
                .archived(false)
                .build());

        return new UploadCourseAttachmentResponse(
                attachment.getId(),
                attachment.getTitle(),
                attachment.getAttachmentType().name(),
                attachment.getFileName(),
                attachment.getResourceUrl()
        );
    }

    public ResponseEntity<Resource> downloadAttachment(String email, Long attachmentId) {
        CourseAttachment attachment = courseAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("Attachment not found."));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found."));
        boolean allowed = switch (user.getRole()) {
            case ADMIN -> true;
            case TEACHER -> courseSectionRepository.findDistinctByTeacherAssignmentsTeacherUserEmailOrderBySectionCodeAsc(email)
                    .stream()
                    .anyMatch(section -> section.getId().equals(attachment.getSection().getId()));
            case STUDENT -> courseSectionRepository.findDistinctByEnrollmentsStudentUserEmailOrderBySectionCodeAsc(email)
                    .stream()
                    .anyMatch(section -> section.getId().equals(attachment.getSection().getId()));
        };
        if (!allowed) {
            throw new ForbiddenException("You do not have access to this attachment.");
        }

        if (attachment.getStoragePath() == null || attachment.getStoragePath().isBlank()) {
            throw new BadRequestException("Attachment does not have a downloadable file.");
        }

        byte[] data = fileStorageService.load(attachment.getStoragePath());
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        attachment.getFileContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : attachment.getFileContentType()
                ))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeFileName(attachment.getFileName()) + "\"")
                .body(resource);
    }

    public ResponseEntity<Resource> downloadSubmissionFile(String email, Long submissionId) {
        CourseAssignmentSubmission submission = courseAssignmentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found."));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found."));

        boolean allowed = switch (user.getRole()) {
            case ADMIN -> true;
            case TEACHER -> submission.getAssignment().getSection().getTeacherAssignments()
                    .stream()
                    .anyMatch(a -> a.getTeacher().getUser().getEmail().equals(email));
            case STUDENT -> submission.getStudent().getUser().getEmail().equals(email);
        };
        if (!allowed) {
            throw new ForbiddenException("You do not have access to this submission file.");
        }

        if (submission.getStoragePath() == null || submission.getStoragePath().isBlank()) {
            throw new BadRequestException("Submission does not have a downloadable file.");
        }

        byte[] data = fileStorageService.load(submission.getStoragePath());
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        submission.getFileContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : submission.getFileContentType()
                ))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeFileName(submission.getFileName()) + "\"")
                .body(resource);
    }

    private CourseSection resolveTeacherSection(String teacherEmail, Long sectionId) {
        if (sectionId == null) {
            throw new BadRequestException("Section is required.");
        }

        CourseSection section = courseSectionRepository.findById(sectionId)
                .orElseThrow(() -> new NotFoundException("Course section not found."));
        boolean ownsSection = section.getTeacherAssignments().stream()
                .anyMatch(assignment -> assignment.getTeacher().getUser().getEmail().equals(teacherEmail));
        if (!ownsSection) {
            throw new ForbiddenException("You can only upload files to your own sections.");
        }
        return section;
    }

    private User findUser(String email, Role role) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found."));
        if (user.getRole() != role) {
            throw new ForbiddenException("Current user role is invalid.");
        }
        return user;
    }

    private TeacherProfile findTeacherProfile(String email) {
        User user = findUser(email, Role.TEACHER);
        if (user.getTeacherProfile() == null) {
            throw new ForbiddenException("Current user is not a teacher.");
        }
        return user.getTeacherProfile();
    }

    private CourseAssignmentResponse toAssignmentResponse(CourseAssignment assignment) {
        TeacherProfile teacher = assignment.getTeacher();
        return new CourseAssignmentResponse(
                assignment.getId(),
                assignment.getSection().getId(),
                assignment.getSection().getSectionCode(),
                assignment.getSection().getCourse().getName(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDate().toString(),
                assignment.getMaxScore(),
                (teacher.getFirstName() + " " + teacher.getLastName()).trim(),
                assignment.getCreatedAt().toString()
        );
    }

    private CourseAssignmentSubmissionResponse toSubmissionResponse(CourseAssignmentSubmission submission) {
        StudentProfile student = submission.getStudent();
        User studentUser = student.getUser();
        TeacherProfile grader = submission.getGradedBy();
        return new CourseAssignmentSubmissionResponse(
                submission.getId(),
                submission.getAssignment().getId(),
                submission.getAssignment().getTitle(),
                studentUser.getId(),
                (student.getFirstName() + " " + student.getLastName()).trim(),
                submission.getTextContent(),
                submission.getFileName(),
                submission.getFileContentType(),
                submission.getLinkUrl(),
                submission.getScore(),
                submission.getTeacherFeedback(),
                submission.getSubmittedAt() == null ? null : submission.getSubmittedAt().toString(),
                submission.getGradedAt() == null ? null : submission.getGradedAt().toString(),
                grader == null ? null : (grader.getFirstName() + " " + grader.getLastName()).trim()
        );
    }

    private AttachmentType parseAttachmentType(String value, String contentType) {
        if (value != null && !value.isBlank()) {
            try {
                return AttachmentType.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid attachment type.");
            }
        }

        if (contentType != null && contentType.startsWith("image/")) return AttachmentType.IMAGE;
        return AttachmentType.DOCUMENT;
    }

    private String safeFileName(String value) {
        if (value == null || value.isBlank()) {
            return "attachment.bin";
        }
        return value.replace("\"", "");
    }

    private String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(label + " is required.");
        }
        return value.trim();
    }

    private Integer requirePositive(Integer value, String label) {
        if (value == null || value < 0) {
            throw new BadRequestException(label + " must be zero or greater.");
        }
        return value;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Due date is required.");
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Due date must be in YYYY-MM-DD format.");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
