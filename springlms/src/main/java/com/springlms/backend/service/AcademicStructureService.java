package com.springlms.backend.service;

import com.springlms.backend.dto.AssignStudentToGroupRequest;
import com.springlms.backend.dto.ChangeStudentGroupApprovalRequest;
import com.springlms.backend.dto.CreateSpecializationRequest;
import com.springlms.backend.dto.CreateStudentGroupRequest;
import com.springlms.backend.dto.GroupStudentResponse;
import com.springlms.backend.dto.SpecializationResponse;
import com.springlms.backend.dto.StudentGroupResponse;
import com.springlms.backend.dto.TeacherStudentOptionResponse;
import com.springlms.backend.model.academicstructure.Faculty;
import com.springlms.backend.model.academicstructure.Specialization;
import com.springlms.backend.model.academicstructure.StudentGroup;
import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.StudentProfile;
import com.springlms.backend.model.user.TeacherProfile;
import com.springlms.backend.model.user.User;
import com.springlms.backend.repository.FacultyRepository;
import com.springlms.backend.repository.SpecializationRepository;
import com.springlms.backend.repository.StudentGroupRepository;
import com.springlms.backend.repository.TeacherProfileRepository;
import com.springlms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicStructureService {

    private final FacultyRepository facultyRepository;
    private final SpecializationRepository specializationRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final UserRepository userRepository;

    public List<SpecializationResponse> listSpecializations() {
        return specializationRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toSpecializationResponse)
                .toList();
    }

    public SpecializationResponse createSpecialization(CreateSpecializationRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Specialization name is required.");
        }

        if (request.facultyId() == null) {
            throw new IllegalArgumentException("Faculty is required.");
        }

        Faculty faculty = facultyRepository.findById(request.facultyId())
                .orElseThrow(() -> new IllegalArgumentException("Faculty not found."));

        String specializationName = request.name().trim();

        if (specializationRepository.existsByNameIgnoreCaseAndFacultyId(specializationName, faculty.getId())) {
            throw new IllegalArgumentException("Specialization already exists in this faculty.");
        }

        TeacherProfile teacherProfile = null;
        if (request.teacherUserId() != null) {
            teacherProfile = teacherProfileRepository.findByUserId(request.teacherUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found."));
        }

        Specialization specialization = Specialization.builder()
                .name(specializationName)
                .faculty(faculty)
                .assignedTeacher(teacherProfile)
                .archived(false)
                .build();

        return toSpecializationResponse(specializationRepository.save(specialization));
    }

    public List<StudentGroupResponse> listStudentGroups() {
        return studentGroupRepository.findAllByOrderByIdDesc()
                .stream()
                .map(this::toStudentGroupResponse)
                .toList();
    }

    public StudentGroupResponse changeStudentGroupApproval(Long groupId, ChangeStudentGroupApprovalRequest request) {
        if (request.approved() == null) {
            throw new IllegalArgumentException("Approval value is required.");
        }

        StudentGroup studentGroup = studentGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Student group not found."));

        studentGroup.setApproved(request.approved());

        return toStudentGroupResponse(studentGroupRepository.save(studentGroup));
    }

    public List<SpecializationResponse> listTeacherSpecializations(String teacherEmail) {
        TeacherProfile teacherProfile = findTeacherProfileByEmail(teacherEmail);

        return specializationRepository.findByAssignedTeacherIdOrderByNameAsc(teacherProfile.getId())
                .stream()
                .map(this::toSpecializationResponse)
                .toList();
    }

    public List<StudentGroupResponse> listTeacherStudentGroups(String teacherEmail) {
        TeacherProfile teacherProfile = findTeacherProfileByEmail(teacherEmail);

        return studentGroupRepository.findByCreatedByTeacherIdOrderByIdDesc(teacherProfile.getId())
                .stream()
                .map(this::toStudentGroupResponse)
                .toList();
    }

    public List<TeacherStudentOptionResponse> listTeacherAssignableStudents(String teacherEmail) {
        findTeacherProfileByEmail(teacherEmail);

        return userRepository.findByRole(Role.STUDENT)
                .stream()
                .filter(user -> user.getStudentProfile() != null)
                .map(this::toTeacherStudentOptionResponse)
                .toList();
    }

    public StudentGroupResponse createTeacherStudentGroup(String teacherEmail, CreateStudentGroupRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Student group name is required.");
        }

        if (request.specializationId() == null) {
            throw new IllegalArgumentException("Specialization is required.");
        }

        TeacherProfile teacherProfile = findTeacherProfileByEmail(teacherEmail);
        Specialization specialization = specializationRepository.findById(request.specializationId())
                .orElseThrow(() -> new IllegalArgumentException("Specialization not found."));

        if (specialization.getAssignedTeacher() == null
                || !specialization.getAssignedTeacher().getId().equals(teacherProfile.getId())) {
            throw new IllegalArgumentException("You can only create student groups for your assigned specializations.");
        }

        String studentGroupName = request.name().trim().toLowerCase();

        if (studentGroupRepository.existsByNameIgnoreCaseAndSpecializationId(studentGroupName, specialization.getId())) {
            throw new IllegalArgumentException("Student group already exists in this specialization.");
        }

        StudentGroup studentGroup = StudentGroup.builder()
                .name(studentGroupName)
                .faculty(specialization.getFaculty())
                .specialization(specialization)
                .createdByTeacher(teacherProfile)
                .approved(false)
                .archived(false)
                .build();

        return toStudentGroupResponse(studentGroupRepository.save(studentGroup));
    }

    public StudentGroupResponse assignStudentToTeacherGroup(
            String teacherEmail,
            Long groupId,
            AssignStudentToGroupRequest request
    ) {
        if (request.studentUserId() == null) {
            throw new IllegalArgumentException("Student is required.");
        }

        TeacherProfile teacherProfile = findTeacherProfileByEmail(teacherEmail);
        StudentGroup studentGroup = studentGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Student group not found."));

        if (!studentGroup.getCreatedByTeacher().getId().equals(teacherProfile.getId())) {
            throw new IllegalArgumentException("You can only manage your own student groups.");
        }

        if (!Boolean.TRUE.equals(studentGroup.getApproved())) {
            throw new IllegalArgumentException("Student group must be approved by admin first.");
        }

        User studentUser = userRepository.findById(request.studentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));

        if (studentUser.getRole() != Role.STUDENT || studentUser.getStudentProfile() == null) {
            throw new IllegalArgumentException("Selected user is not a student.");
        }

        StudentProfile studentProfile = studentUser.getStudentProfile();
        studentProfile.setFaculty(studentGroup.getFaculty());
        studentProfile.setGroup(studentGroup);
        studentUser.setStudentProfile(studentProfile);

        userRepository.save(studentUser);

        return toStudentGroupResponse(studentGroup);
    }

    private TeacherProfile findTeacherProfileByEmail(String teacherEmail) {
        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found."));

        if (user.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("Current user is not a teacher.");
        }

        return teacherProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher profile not found."));
    }

    private SpecializationResponse toSpecializationResponse(Specialization specialization) {
        TeacherProfile assignedTeacher = specialization.getAssignedTeacher();

        return new SpecializationResponse(
                specialization.getId(),
                specialization.getName(),
                specialization.getFaculty().getId(),
                specialization.getFaculty().getName(),
                assignedTeacher != null ? assignedTeacher.getUser().getId() : null,
                assignedTeacher != null ? assignedTeacher.getUser().getEmail() : null,
                specialization.getArchived()
        );
    }

    private StudentGroupResponse toStudentGroupResponse(StudentGroup studentGroup) {
        return new StudentGroupResponse(
                studentGroup.getId(),
                studentGroup.getName(),
                studentGroup.getSpecialization().getId(),
                studentGroup.getSpecialization().getName(),
                studentGroup.getFaculty().getId(),
                studentGroup.getFaculty().getName(),
                studentGroup.getCreatedByTeacher().getUser().getId(),
                studentGroup.getCreatedByTeacher().getUser().getEmail(),
                studentGroup.getApproved(),
                studentGroup.getArchived(),
                userRepository.findByStudentProfile_Group_IdOrderByStudentProfileFirstNameAscStudentProfileLastNameAsc(studentGroup.getId())
                        .stream()
                        .map(this::toGroupStudentResponse)
                        .toList()
        );
    }

    private TeacherStudentOptionResponse toTeacherStudentOptionResponse(User user) {
        StudentProfile studentProfile = user.getStudentProfile();

        return new TeacherStudentOptionResponse(
                user.getId(),
                user.getEmail(),
                studentProfile.getFirstName(),
                studentProfile.getLastName(),
                user.getState(),
                studentProfile.getGroup() != null ? studentProfile.getGroup().getName() : null
        );
    }

    private GroupStudentResponse toGroupStudentResponse(User user) {
        StudentProfile studentProfile = user.getStudentProfile();

        return new GroupStudentResponse(
                user.getId(),
                user.getEmail(),
                studentProfile.getFirstName(),
                studentProfile.getLastName(),
                user.getState()
        );
    }
}
