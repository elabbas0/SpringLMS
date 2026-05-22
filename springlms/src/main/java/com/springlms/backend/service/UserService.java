package com.springlms.backend.service;

import com.springlms.backend.dto.AdminDashboardResponse;
import com.springlms.backend.dto.CreateTeacherRequest;
import com.springlms.backend.dto.LoginRequest;
import com.springlms.backend.dto.LoginResponse;
import com.springlms.backend.dto.StudentRegisterRequest;
import com.springlms.backend.dto.UserResponse;
import com.springlms.backend.model.academicstructure.Faculty;
import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.State;
import com.springlms.backend.model.user.StudentProfile;
import com.springlms.backend.model.user.TeacherProfile;
import com.springlms.backend.model.user.User;
import com.springlms.backend.repository.FacultyRepository;
import com.springlms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse registerStudent(StudentRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.STUDENT)
                .state(State.PENDING_APPROVAL)
                .build();

        StudentProfile studentProfile = StudentProfile.builder()
                .studentNumber(request.studentNumber())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .emergencyContactName(request.emergencyContactName())
                .emergencyContactPhone(request.emergencyContactPhone())
                .emergencyContactRelation(request.emergencyContactRelation())
                .build();

        user.setStudentProfile(studentProfile);

        User savedUser = registerUser(user);

        return toUserResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getState(),
                "Login successful. JWT will be added later."
        );
    }

    public UserResponse createTeacher(CreateTeacherRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        Faculty faculty = facultyRepository.findById(request.facultyId())
                .orElseThrow(() -> new IllegalArgumentException("Faculty not found."));

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.TEACHER)
                .state(State.ACTIVE)
                .build();

        TeacherProfile teacherProfile = TeacherProfile.builder()
                .employeeNumber(request.employeeNumber())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .faculty(faculty)
                .hoursTaught(request.hoursTaught() == null ? 0 : request.hoursTaught())
                .build();

        user.setTeacherProfile(teacherProfile);

        validateUserProfileByRole(user);

        if (user.getTeacherProfile() != null) {
            user.getTeacherProfile().setUser(user);
        }

        User savedUser = userRepository.save(user);

        return toUserResponse(savedUser);
    }

    public AdminDashboardResponse getAdminDashboard() {
        long totalUsers = userRepository.count();
        long totalStudents = userRepository.findByRole(Role.STUDENT).size();
        long totalTeachers = userRepository.findByRole(Role.TEACHER).size();
        long totalAdmins = userRepository.findByRole(Role.ADMIN).size();
        long pendingUsers = userRepository.findAll()
                .stream()
                .filter(user -> user.getState() == State.PENDING_APPROVAL)
                .count();

        return new AdminDashboardResponse(
                totalUsers,
                totalStudents,
                totalTeachers,
                totalAdmins,
                pendingUsers
        );
    }

    public User registerUser(User user) {
        validateForRegistration(user);

        user.setState(State.PENDING_APPROVAL);

        if (user.getStudentProfile() != null) {
            user.getStudentProfile().setUser(user);
        }

        if (user.getTeacherProfile() != null) {
            user.getTeacherProfile().setUser(user);
        }

        return userRepository.save(user);
    }

    public User approveUser(User user) {
        validateUserProfileByRole(user);

        user.setState(State.ACTIVE);

        if (user.getStudentProfile() != null) {
            user.getStudentProfile().setUser(user);
        }

        if (user.getTeacherProfile() != null) {
            user.getTeacherProfile().setUser(user);
        }

        return userRepository.save(user);
    }

    public User updateUser(User user) {
        if (user.getState() == State.ACTIVE) {
            validateUserProfileByRole(user);
        }

        if (user.getStudentProfile() != null) {
            user.getStudentProfile().setUser(user);
        }

        if (user.getTeacherProfile() != null) {
            user.getTeacherProfile().setUser(user);
        }

        return userRepository.save(user);
    }

    private void validateForRegistration(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (user.getRole() == null) {
            throw new IllegalArgumentException("User role is required.");
        }

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Admin users cannot self-register.");
        }

        if (user.getStudentProfile() != null && user.getTeacherProfile() != null) {
            throw new IllegalArgumentException("A user cannot register with both student and teacher profiles.");
        }

        if (user.getRole() == Role.STUDENT && user.getTeacherProfile() != null) {
            throw new IllegalArgumentException("Student users cannot have a teacher profile.");
        }

        if (user.getRole() == Role.TEACHER && user.getStudentProfile() != null) {
            throw new IllegalArgumentException("Teacher users cannot have a student profile.");
        }
    }

    private void validateUserProfileByRole(User user) {
        if (user.getRole() == Role.ADMIN) {
            if (user.getStudentProfile() != null || user.getTeacherProfile() != null) {
                throw new IllegalArgumentException("Admin users cannot have any other profiles.");
            }
        }

        if (user.getRole() == Role.STUDENT) {
            if (user.getStudentProfile() == null) {
                throw new IllegalArgumentException("Student users must have a student profile.");
            }

            if (user.getTeacherProfile() != null) {
                throw new IllegalArgumentException("Student users cannot have a teacher profile.");
            }

            validateStudentProfile(user.getStudentProfile());
        }

        if (user.getRole() == Role.TEACHER) {
            if (user.getTeacherProfile() == null) {
                throw new IllegalArgumentException("Teacher users must have a teacher profile.");
            }

            if (user.getStudentProfile() != null) {
                throw new IllegalArgumentException("Teacher users cannot have a student profile.");
            }

            validateTeacherProfile(user.getTeacherProfile());
        }
    }

    private void validateStudentProfile(StudentProfile profile) {
        if (profile.getStudentNumber() == null || profile.getStudentNumber().isBlank()) {
            throw new IllegalArgumentException("Student number is required.");
        }

        if (profile.getFirstName() == null || profile.getFirstName().isBlank()) {
            throw new IllegalArgumentException("Student first name is required.");
        }

        if (profile.getLastName() == null || profile.getLastName().isBlank()) {
            throw new IllegalArgumentException("Student last name is required.");
        }

        if (profile.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Student date of birth is required.");
        }

        if (profile.getPhoneNumber() == null || profile.getPhoneNumber().isBlank()) {
            throw new IllegalArgumentException("Student phone number is required.");
        }

        if (profile.getAddress() == null || profile.getAddress().isBlank()) {
            throw new IllegalArgumentException("Student address is required.");
        }

        if (profile.getFaculty() == null) {
            throw new IllegalArgumentException("Student faculty is required.");
        }

        if (profile.getGroup() == null) {
            throw new IllegalArgumentException("Student group is required.");
        }

        if (profile.getGroup().getFaculty() == null) {
            throw new IllegalArgumentException("Student group must belong to a faculty.");
        }

        if (!profile.getGroup().getFaculty().getId().equals(profile.getFaculty().getId())) {
            throw new IllegalArgumentException("Student group must belong to the selected faculty.");
        }
    }

    private void validateTeacherProfile(TeacherProfile profile) {
        if (profile.getEmployeeNumber() == null || profile.getEmployeeNumber().isBlank()) {
            throw new IllegalArgumentException("Employee number is required.");
        }

        if (profile.getFirstName() == null || profile.getFirstName().isBlank()) {
            throw new IllegalArgumentException("Teacher first name is required.");
        }

        if (profile.getLastName() == null || profile.getLastName().isBlank()) {
            throw new IllegalArgumentException("Teacher last name is required.");
        }

        if (profile.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Teacher date of birth is required.");
        }

        if (profile.getPhoneNumber() == null || profile.getPhoneNumber().isBlank()) {
            throw new IllegalArgumentException("Teacher phone number is required.");
        }

        if (profile.getAddress() == null || profile.getAddress().isBlank()) {
            throw new IllegalArgumentException("Teacher address is required.");
        }

        if (profile.getFaculty() == null) {
            throw new IllegalArgumentException("Teacher faculty is required.");
        }
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getState()
        );
    }
}