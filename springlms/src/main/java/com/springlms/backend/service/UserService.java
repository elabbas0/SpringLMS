package com.springlms.backend.service;

import com.springlms.backend.dto.AdminDashboardResponse;
import com.springlms.backend.dto.ChangeUserStateRequest;
import com.springlms.backend.dto.CreateFacultyRequest;
import com.springlms.backend.dto.CreateTeacherRequest;
import com.springlms.backend.dto.FacultyResponse;
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
import com.springlms.backend.security.JwtService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Pattern FIN_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{7}$");
    private static final Pattern SERIAL_NUMBER_PATTERN = Pattern.compile("^[A-Z]{2,4}[0-9]{6,10}$");

    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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
                .finCode(normalizeIdentityValue(request.finCode()))
                .serialNumber(normalizeIdentityValue(request.serialNumber()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .entranceScore(request.entranceScore())
                .studyYear(request.studyYear())
                .admissionYear(request.admissionYear())
                .fundingType(request.fundingType())
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

        if (user.getState() == State.PENDING_APPROVAL) {
            throw new IllegalArgumentException("Your account is pending admin approval.");
        }

        if (user.getState() != State.ACTIVE) {
            throw new IllegalArgumentException("Your account is not active.");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getState(),
                token,
                "Bearer"
        );
    }

    public UserResponse createTeacher(CreateTeacherRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        if (request.facultyId() == null) {
            throw new IllegalArgumentException("Teacher faculty is required.");
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
                .finCode(normalizeIdentityValue(request.finCode()))
                .serialNumber(normalizeIdentityValue(request.serialNumber()))
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

    public List<FacultyResponse> listFaculties() {
        return facultyRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toFacultyResponse)
                .toList();
    }

    public FacultyResponse createFaculty(CreateFacultyRequest request) {
        String facultyName = normalizeText(request.name());
        String facultyCode = normalizeIdentityValue(request.code());

        if (facultyName == null || facultyName.isBlank()) {
            throw new IllegalArgumentException("Faculty name is required.");
        }

        if (facultyCode == null || facultyCode.isBlank()) {
            throw new IllegalArgumentException("Faculty code is required.");
        }

        if (facultyRepository.existsByNameIgnoreCase(facultyName)) {
            throw new IllegalArgumentException("Faculty name already exists.");
        }

        if (facultyRepository.existsByCode(facultyCode)) {
            throw new IllegalArgumentException("Faculty code already exists.");
        }

        Faculty faculty = Faculty.builder()
                .name(facultyName)
                .code(facultyCode)
                .archived(false)
                .build();

        Faculty savedFaculty = facultyRepository.save(faculty);

        return toFacultyResponse(savedFaculty);
    }

    public AdminDashboardResponse getAdminDashboard() {
        long totalUsers = userRepository.count();
        long totalStudents = userRepository.countByRole(Role.STUDENT);
        long totalTeachers = userRepository.countByRole(Role.TEACHER);
        long totalAdmins = userRepository.countByRole(Role.ADMIN);
        long pendingUsers = userRepository.countByState(State.PENDING_APPROVAL);
        long activeUsers = userRepository.countByState(State.ACTIVE);
        long inactiveUsers = userRepository.countByState(State.INACTIVE);
        long suspendedUsers = userRepository.countByState(State.SUSPENDED);
        long academicLeaveUsers = userRepository.countByState(State.ACADEMIC_LEAVE);
        long graduatedUsers = userRepository.countByState(State.GRADUATED);
        long dismissedUsers = userRepository.countByState(State.DISMISSED);

        return new AdminDashboardResponse(
                totalUsers,
                totalStudents,
                totalTeachers,
                totalAdmins,
                pendingUsers,
                activeUsers,
                inactiveUsers,
                suspendedUsers,
                academicLeaveUsers,
                graduatedUsers,
                dismissedUsers
        );
    }

    public List<UserResponse> searchUsers(String query, Role role, State state) {
        Specification<User> specification = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.isBlank()) {
                String searchPattern = "%" + query.trim().toLowerCase() + "%";

                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        searchPattern
                ));
            }

            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }

            if (state != null) {
                predicates.add(criteriaBuilder.equal(root.get("state"), state));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(specification)
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    public UserResponse changeUserState(Long userId, ChangeUserStateRequest request) {
        if (request.state() == null) {
            throw new IllegalArgumentException("User state is required.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (request.state() == State.ACTIVE) {
            validateUserProfileByRole(user);
        }

        user.setState(request.state());

        User savedUser = userRepository.save(user);

        return toUserResponse(savedUser);
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
        if (profile.getFinCode() == null || profile.getFinCode().isBlank()) {
            throw new IllegalArgumentException("FIN code is required.");
        }

        if (!FIN_CODE_PATTERN.matcher(profile.getFinCode()).matches()) {
            throw new IllegalArgumentException("FIN code must be 7 uppercase letters or digits.");
        }

        if (profile.getSerialNumber() == null || profile.getSerialNumber().isBlank()) {
            throw new IllegalArgumentException("Serial number is required.");
        }

        if (!SERIAL_NUMBER_PATTERN.matcher(profile.getSerialNumber()).matches()) {
            throw new IllegalArgumentException("Serial number must start with letters and end with digits.");
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

        if (profile.getEntranceScore() == null) {
            throw new IllegalArgumentException("Entrance score is required.");
        }

        if (profile.getEntranceScore() < 0) {
            throw new IllegalArgumentException("Entrance score cannot be negative.");
        }

        if (profile.getStudyYear() == null) {
            throw new IllegalArgumentException("Study year is required.");
        }

        if (profile.getStudyYear() < 1) {
            throw new IllegalArgumentException("Study year must be at least 1.");
        }

        if (profile.getAdmissionYear() == null) {
            throw new IllegalArgumentException("Admission year is required.");
        }

        if (profile.getAdmissionYear() < 1900) {
            throw new IllegalArgumentException("Admission year is invalid.");
        }

        if (profile.getFundingType() == null) {
            throw new IllegalArgumentException("Funding type is required.");
        }

        if (profile.getFaculty() != null || profile.getGroup() != null) {
            if (profile.getFaculty() == null) {
                throw new IllegalArgumentException("Student faculty is required when a group is assigned.");
            }

            if (profile.getGroup() == null) {
                throw new IllegalArgumentException("Student group is required when a faculty is assigned.");
            }

            if (profile.getGroup().getFaculty() == null) {
                throw new IllegalArgumentException("Student group must belong to a faculty.");
            }

            if (!profile.getGroup().getFaculty().getId().equals(profile.getFaculty().getId())) {
                throw new IllegalArgumentException("Student group must belong to the selected faculty.");
            }
        }
    }

    private void validateTeacherProfile(TeacherProfile profile) {
        if (profile.getFinCode() == null || profile.getFinCode().isBlank()) {
            throw new IllegalArgumentException("Teacher FIN code is required.");
        }

        if (!FIN_CODE_PATTERN.matcher(profile.getFinCode()).matches()) {
            throw new IllegalArgumentException("Teacher FIN code must be 7 uppercase letters or digits.");
        }

        if (profile.getSerialNumber() == null || profile.getSerialNumber().isBlank()) {
            throw new IllegalArgumentException("Teacher serial number is required.");
        }

        if (!SERIAL_NUMBER_PATTERN.matcher(profile.getSerialNumber()).matches()) {
            throw new IllegalArgumentException("Teacher serial number must start with letters and end with digits.");
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
        StudentProfile studentProfile = user.getStudentProfile();

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getState(),
                studentProfile != null ? studentProfile.getFirstName() : null,
                studentProfile != null ? studentProfile.getLastName() : null,
                studentProfile != null ? studentProfile.getEntranceScore() : null,
                studentProfile != null ? studentProfile.getStudyYear() : null,
                studentProfile != null ? studentProfile.getAdmissionYear() : null,
                studentProfile != null ? studentProfile.getFundingType() : null
        );
    }

    private FacultyResponse toFacultyResponse(Faculty faculty) {
        return new FacultyResponse(
                faculty.getId(),
                faculty.getName(),
                faculty.getCode(),
                faculty.getArchived()
        );
    }

    private String normalizeIdentityValue(String value) {
        if (value == null) {
            return null;
        }

        return value
                .trim()
                .replace(" ", "")
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        return value.trim();
    }
}
