package com.springlms.backend.service;

import com.springlms.backend.dto.*;
import com.springlms.backend.model.academicstructure.AcademicTerm;
import com.springlms.backend.model.academicstructure.StudentGroup;
import com.springlms.backend.model.course.*;
import com.springlms.backend.model.enrollment.SectionEnrollment;
import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.StudentProfile;
import com.springlms.backend.model.user.TeacherProfile;
import com.springlms.backend.model.user.User;
import com.springlms.backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseMaterialService {

    private final CourseRepository courseRepository;
    private final AcademicTermRepository academicTermRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final CourseSectionGroupRepository courseSectionGroupRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final SectionEnrollmentRepository sectionEnrollmentRepository;
    private final CourseAttachmentRepository courseAttachmentRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final UserRepository userRepository;

    public List<CourseResponse> listCourses() {
        return courseRepository.findByArchivedFalseOrderByNameAsc()
                .stream()
                .map(this::toCourseResponse)
                .toList();
    }

    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        String code = requireText(request.code(), "Course code").toUpperCase();
        String name = requireText(request.name(), "Course name");
        int credits = requirePositive(request.credits(), "Course credits");

        Course course = courseRepository.findByCodeIgnoreCase(code)
                .orElseGet(() -> Course.builder()
                        .code(code)
                        .name(name)
                        .credits(credits)
                        .build());

        course.setCode(code);
        course.setName(name);
        course.setDescription(trimToNull(request.description()));
        course.setCredits(credits);
        course.setArchived(false);

        return toCourseResponse(courseRepository.save(course));
    }

    public List<CourseSectionResponse> listAdminSections() {
        return courseSectionRepository.findByArchivedFalseOrderBySectionCodeAsc()
                .stream()
                .map(this::toSectionResponse)
                .toList();
    }

    public List<CourseSectionResponse> listTeacherSections(String teacherEmail) {
        List<Long> sectionIds = teacherAssignmentRepository.findByTeacherUserEmailOrderByIdAsc(teacherEmail)
                .stream()
                .map(assignment -> assignment.getSection().getId())
                .distinct()
                .toList();

        return courseSectionRepository.findAllById(sectionIds)
                .stream()
                .map(this::toSectionResponse)
                .toList();
    }

    public List<CourseSectionResponse> listStudentSections(String studentEmail) {
        return courseSectionRepository.findDistinctByEnrollmentsStudentUserEmailOrderBySectionCodeAsc(studentEmail)
                .stream()
                .map(this::toSectionResponse)
                .toList();
    }

    public List<CourseAttachmentResponse> listTeacherAttachments(String teacherEmail) {
        List<Long> sectionIds = teacherAssignmentRepository.findByTeacherUserEmailOrderByIdAsc(teacherEmail)
                .stream()
                .map(assignment -> assignment.getSection().getId())
                .distinct()
                .toList();

        return courseAttachmentRepository.findBySectionIdInOrderByCreatedAtDesc(sectionIds)
                .stream()
                .map(this::toAttachmentResponse)
                .toList();
    }

    public List<StudentMaterialSectionResponse> listStudentMaterials(String studentEmail) {
        List<CourseSection> sections = courseSectionRepository.findDistinctByEnrollmentsStudentUserEmailOrderBySectionCodeAsc(studentEmail);
        List<Long> sectionIds = sections.stream().map(CourseSection::getId).toList();

        return sections.stream()
                .map(section -> new StudentMaterialSectionResponse(
                        section.getId(),
                        section.getCourse().getCode(),
                        section.getCourse().getName(),
                        section.getSectionCode(),
                        courseAttachmentRepository.findBySectionIdInOrderByCreatedAtDesc(List.of(section.getId()))
                                .stream()
                                .map(this::toAttachmentResponse)
                                .toList()
                ))
                .toList();
    }

    @Transactional
    public CourseSectionResponse createSection(String adminEmail, CreateCourseSectionRequest request) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found."));

        AcademicTerm term = getOrCreateActiveTerm();
        String sectionCode = requireText(request.sectionCode(), "Section code").toUpperCase();

        CourseSection section = courseSectionRepository.findByArchivedFalseOrderBySectionCodeAsc()
                .stream()
                .filter(current -> current.getCourse().getId().equals(course.getId())
                        && current.getAcademicTerm().getId().equals(term.getId())
                        && current.getSectionCode().equalsIgnoreCase(sectionCode))
                .findFirst()
                .orElseGet(() -> CourseSection.builder()
                        .course(course)
                        .academicTerm(term)
                        .sectionCode(sectionCode)
                        .build());

        section.setCourse(course);
        section.setAcademicTerm(term);
        section.setSectionCode(sectionCode);
        section.setArchived(false);

        CourseSection savedSection = courseSectionRepository.save(section);

        savedSection.getTeacherAssignments().clear();
        savedSection.getGroups().clear();
        savedSection.getEnrollments().clear();

        if (request.teacherAssignments() == null || request.teacherAssignments().isEmpty()) {
            throw new IllegalArgumentException("At least one teacher assignment is required.");
        }

        for (CreateCourseSectionTeacherAssignmentRequest assignmentRequest : request.teacherAssignments()) {
            if (assignmentRequest == null || assignmentRequest.teacherUserId() == null || assignmentRequest.assignmentType() == null) {
                continue;
            }

            User teacherUser = userRepository.findById(assignmentRequest.teacherUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found."));
            if (teacherUser.getRole() != Role.TEACHER || teacherUser.getTeacherProfile() == null) {
                throw new IllegalArgumentException("Selected user is not a teacher.");
            }

            TeacherAssignment teacherAssignment = TeacherAssignment.builder()
                    .section(savedSection)
                    .teacher(teacherUser.getTeacherProfile())
                    .assignmentType(assignmentRequest.assignmentType())
                    .build();

            teacherAssignmentRepository.save(teacherAssignment);
            savedSection.getTeacherAssignments().add(teacherAssignment);
        }

        if (request.groupIds() != null && !request.groupIds().isEmpty()) {
            for (Long groupId : request.groupIds()) {
                StudentGroup studentGroup = studentGroupRepository.findById(groupId)
                        .orElseThrow(() -> new IllegalArgumentException("Student group not found."));

                CourseSectionGroup sectionGroup = CourseSectionGroup.builder()
                        .section(savedSection)
                        .group(studentGroup)
                        .build();
                courseSectionGroupRepository.save(sectionGroup);
                savedSection.getGroups().add(sectionGroup);

                enrollStudentsFromGroup(savedSection, studentGroup);
            }
        }

        return toSectionResponse(savedSection);
    }

    @Transactional
    public CourseAttachmentResponse createAttachment(String teacherEmail, CreateCourseAttachmentRequest request) {
        TeacherProfile teacher = findTeacherByEmail(teacherEmail);
        CourseSection section = courseSectionRepository.findById(request.sectionId())
                .orElseThrow(() -> new IllegalArgumentException("Course section not found."));

        boolean ownsSection = teacherAssignmentRepository.findBySectionId(section.getId())
                .stream()
                .anyMatch(assignment -> assignment.getTeacher().getId().equals(teacher.getId()));

        if (!ownsSection) {
            throw new IllegalArgumentException("You can only add attachments to your own sections.");
        }

        if (request.attachmentType() == null) {
            throw new IllegalArgumentException("Attachment type is required.");
        }

        String title = requireText(request.title(), "Attachment title");
        String resourceUrl = requireText(request.resourceUrl(), "Attachment link");

        CourseAttachment attachment = courseAttachmentRepository.save(CourseAttachment.builder()
                .section(section)
                .teacher(teacher)
                .attachmentType(request.attachmentType())
                .title(title)
                .resourceUrl(resourceUrl)
                .description(trimToNull(request.description()))
                .build());

        return toAttachmentResponse(attachment);
    }

    private void enrollStudentsFromGroup(CourseSection section, StudentGroup studentGroup) {
        List<User> students = userRepository.findByStudentProfile_Group_IdOrderByStudentProfileFirstNameAscStudentProfileLastNameAsc(studentGroup.getId());

        for (User student : students) {
            StudentProfile studentProfile = student.getStudentProfile();
            if (sectionEnrollmentRepository.findByStudentIdAndSectionId(studentProfile.getId(), section.getId()).isPresent()) {
                continue;
            }

            SectionEnrollment enrollment = SectionEnrollment.builder()
                    .student(studentProfile)
                    .section(section)
                    .active(true)
                    .build();
            sectionEnrollmentRepository.save(enrollment);
            section.getEnrollments().add(enrollment);
        }
    }

    private AcademicTerm getOrCreateActiveTerm() {
        return academicTermRepository.findFirstByActiveTrueAndArchivedFalseOrderByStartDateDesc()
                .orElseGet(() -> academicTermRepository.save(AcademicTerm.builder()
                        .name(LocalDate.now().getYear() + " Spring")
                        .startDate(LocalDate.now().withMonth(2).withDayOfMonth(1))
                        .endDate(LocalDate.now().withMonth(7).withDayOfMonth(31))
                        .active(true)
                        .archived(false)
                        .build()));
    }

    private TeacherProfile findTeacherByEmail(String teacherEmail) {
        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found."));

        if (user.getRole() != Role.TEACHER || user.getTeacherProfile() == null) {
            throw new IllegalArgumentException("Current user is not a teacher.");
        }

        return user.getTeacherProfile();
    }

    private CourseResponse toCourseResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCode(),
                course.getName(),
                course.getDescription(),
                course.getCredits(),
                course.getArchived()
        );
    }

    private CourseSectionResponse toSectionResponse(CourseSection section) {
        List<CourseSectionGroupResponse> groups = courseSectionGroupRepository.findBySectionId(section.getId())
                .stream()
                .map(group -> new CourseSectionGroupResponse(group.getGroup().getId(), group.getGroup().getName()))
                .toList();

        List<CourseSectionTeacherAssignmentResponse> teacherAssignments = teacherAssignmentRepository.findBySectionId(section.getId())
                .stream()
                .map(assignment -> {
                    TeacherProfile teacher = assignment.getTeacher();
                    String teacherName = (teacher.getFirstName() + " " + teacher.getLastName()).trim();
                    return new CourseSectionTeacherAssignmentResponse(
                            assignment.getId(),
                            teacher.getUser().getId(),
                            teacher.getUser().getEmail(),
                            teacherName,
                            assignment.getAssignmentType().name()
                    );
                })
                .toList();

        List<CourseAttachmentResponse> attachments = courseAttachmentRepository.findBySectionIdOrderByCreatedAtDesc(section.getId())
                .stream()
                .map(this::toAttachmentResponse)
                .toList();

        Integer enrollmentCount = sectionEnrollmentRepository.findBySectionId(section.getId()).size();

        return new CourseSectionResponse(
                section.getId(),
                section.getCourse().getId(),
                section.getCourse().getCode(),
                section.getCourse().getName(),
                section.getSectionCode(),
                section.getAcademicTerm().getName(),
                groups,
                teacherAssignments,
                attachments,
                enrollmentCount
        );
    }

    private CourseAttachmentResponse toAttachmentResponse(CourseAttachment attachment) {
        TeacherProfile teacher = attachment.getTeacher();
        String teacherName = (teacher.getFirstName() + " " + teacher.getLastName()).trim();

        return new CourseAttachmentResponse(
                attachment.getId(),
                attachment.getSection().getId(),
                attachment.getSection().getSectionCode(),
                attachment.getSection().getCourse().getName(),
                attachment.getTitle(),
                attachment.getAttachmentType().name(),
                attachment.getResourceUrl(),
                attachment.getDescription(),
                teacherName,
                attachment.getCreatedAt().toString()
        );
    }

    private String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value.trim();
    }

    private int requirePositive(Integer value, String label) {
        if (value == null || value < 1) {
            throw new IllegalArgumentException(label + " must be at least 1.");
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
