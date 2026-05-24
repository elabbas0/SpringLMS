package com.springlms.backend.config;

import com.springlms.backend.model.academicstructure.Faculty;
import com.springlms.backend.model.academicstructure.Specialization;
import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.State;
import com.springlms.backend.model.user.StudentProfile;
import com.springlms.backend.model.user.TeacherProfile;
import com.springlms.backend.model.user.User;
import com.springlms.backend.repository.FacultyRepository;
import com.springlms.backend.repository.SpecializationRepository;
import com.springlms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private static final String TEACHER_EMAIL = "teacher.demo@springlms.local";
    private static final String STUDENT_ONE_EMAIL = "student.one@springlms.local";
    private static final String STUDENT_TWO_EMAIL = "student.two@springlms.local";
    private static final String DEMO_PASSWORD = "Demo1234";

    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final SpecializationRepository specializationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Faculty faculty = ensureFaculty();
        User teacher = ensureTeacher(faculty);
        ensureSpecialization(faculty, teacher);
        ensureStudents();

        System.out.println();
        System.out.println("==============================================");
        System.out.println(" DEMO ACCOUNTS READY");
        System.out.println(" Teacher: " + TEACHER_EMAIL + " / " + DEMO_PASSWORD);
        System.out.println(" Student: " + STUDENT_ONE_EMAIL + " / " + DEMO_PASSWORD + " (ACTIVE)");
        System.out.println(" Student: " + STUDENT_TWO_EMAIL + " / " + DEMO_PASSWORD + " (PENDING_APPROVAL)");
        System.out.println("==============================================");
        System.out.println();
    }

    private Faculty ensureFaculty() {
        return facultyRepository.findAllByOrderByNameAsc()
                .stream()
                .filter(faculty -> "ITT".equalsIgnoreCase(faculty.getCode()))
                .findFirst()
                .orElseGet(() -> facultyRepository.save(Faculty.builder()
                        .name("Information Technologies and Telecommunications")
                        .code("ITT")
                        .archived(false)
                        .build()));
    }

    private User ensureTeacher(Faculty faculty) {
        return userRepository.findByEmail(TEACHER_EMAIL)
                .orElseGet(() -> {
                    User teacher = User.builder()
                            .email(TEACHER_EMAIL)
                            .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                            .role(Role.TEACHER)
                            .state(State.ACTIVE)
                            .build();

                    TeacherProfile teacherProfile = TeacherProfile.builder()
                            .finCode("TEA1234")
                            .serialNumber("AZE7654321")
                            .firstName("Demo")
                            .lastName("Teacher")
                            .dateOfBirth(LocalDate.of(1990, 5, 12))
                            .phoneNumber("0500000001")
                            .address("Baku")
                            .faculty(faculty)
                            .hoursTaught(12)
                            .build();

                    teacher.setTeacherProfile(teacherProfile);
                    teacherProfile.setUser(teacher);

                    return userRepository.save(teacher);
                });
    }

    private void ensureSpecialization(Faculty faculty, User teacher) {
        boolean exists = specializationRepository.findAllByOrderByNameAsc()
                .stream()
                .anyMatch(specialization ->
                        "Computer Science".equalsIgnoreCase(specialization.getName())
                                && specialization.getFaculty().getId().equals(faculty.getId()));

        if (exists) {
            return;
        }

        specializationRepository.save(Specialization.builder()
                .name("Computer Science")
                .faculty(faculty)
                .assignedTeacher(teacher.getTeacherProfile())
                .archived(false)
                .build());
    }

    private void ensureStudents() {
        ensureStudent(
                STUDENT_ONE_EMAIL,
                "STU1234",
                "AZE1111111",
                "Student",
                "One",
                State.ACTIVE
        );

        ensureStudent(
                STUDENT_TWO_EMAIL,
                "STU5678",
                "AZE2222222",
                "Student",
                "Two",
                State.PENDING_APPROVAL
        );
    }

    private void ensureStudent(
            String email,
            String finCode,
            String serialNumber,
            String firstName,
            String lastName,
            State state
    ) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        User student = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                .role(Role.STUDENT)
                .state(state)
                .build();

        StudentProfile studentProfile = StudentProfile.builder()
                .finCode(finCode)
                .serialNumber(serialNumber)
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(LocalDate.of(2004, 3, 14))
                .phoneNumber("0500000002")
                .address("Baku")
                .emergencyContactName("Parent")
                .emergencyContactPhone("0500000003")
                .emergencyContactRelation("Parent")
                .build();

        student.setStudentProfile(studentProfile);
        studentProfile.setUser(student);

        userRepository.save(student);
    }
}
