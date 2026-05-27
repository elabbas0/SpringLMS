package com.springlms.backend.config;

import com.springlms.backend.model.academicstructure.Faculty;
import com.springlms.backend.model.academicstructure.Specialization;
import com.springlms.backend.model.academicstructure.StudentGroup;
import com.springlms.backend.model.academicstructure.StudentGroupTeacherAssignment;
import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.State;
import com.springlms.backend.model.user.StudentFundingType;
import com.springlms.backend.model.user.StudentProfile;
import com.springlms.backend.model.user.TeacherProfile;
import com.springlms.backend.model.user.User;
import com.springlms.backend.repository.FacultyRepository;
import com.springlms.backend.repository.SpecializationRepository;
import com.springlms.backend.repository.StudentGroupRepository;
import com.springlms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "Demo1234";
    private static final String[] GROUP_NAMES = {"6525a1", "6525a2", "6525a3", "6525a4"};
    private static final String[] SUBJECT_NAMES = {
            "Riyazi analiz",
            "Komputer qrafikasi",
            "Proqramlasdirma",
            "Diskret riyaziyyat",
            "Fizika"
    };

    private static final int[] SUBJECT_CREDITS = {7, 5, 5, 6, 7};
    private static final int[] SUBJECT_LECTURES = {1, 1, 1, 1, 1};
    private static final int[] SUBJECT_SEMINARS = {1, 0, 0, 1, 0};
    private static final int[] SUBJECT_LABS = {0, 1, 1, 0, 1};

    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final SpecializationRepository specializationRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Faculty faculty = ensureFaculty();
        List<User> teachers = ensureTeachers(faculty);
        Specialization specialization = ensureSpecialization(faculty, teachers.getFirst());
        List<StudentGroup> studentGroups = ensureStudentGroups(specialization, teachers);
        ensureStudentsForGroups(studentGroups);

        System.out.println();
        System.out.println("==============================================");
        System.out.println(" DEMO DATA READY");
        System.out.println(" Password for all demo users: " + DEMO_PASSWORD);
        System.out.println(" Teachers seeded: 5");
        System.out.println(" Groups seeded: 6525a1, 6525a2, 6525a3, 6525a4");
        System.out.println(" Students seeded: 20 per group");
        System.out.println(" Example teacher: teacher.demo1@springlms.local / " + DEMO_PASSWORD);
        System.out.println(" Example student: student.6525a1.01@springlms.local / " + DEMO_PASSWORD);
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

    private List<User> ensureTeachers(Faculty faculty) {
        return List.of(
                ensureTeacher(faculty, 1, "Demo", "Teacher", 14),
                ensureTeacher(faculty, 2, "Leyla", "Mammadova", 16),
                ensureTeacher(faculty, 3, "Rashad", "Aliyev", 18),
                ensureTeacher(faculty, 4, "Nigar", "Hasanova", 20),
                ensureTeacher(faculty, 5, "Murad", "Karimov", 22)
        );
    }

    private User ensureTeacher(Faculty faculty, int index, String firstName, String lastName, int hoursTaught) {
        String email = "teacher.demo" + index + "@springlms.local";

        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User teacher = User.builder()
                            .email(email)
                            .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                            .role(Role.TEACHER)
                            .state(State.ACTIVE)
                            .build();

                    TeacherProfile teacherProfile = TeacherProfile.builder()
                            .finCode(buildTeacherFin(index))
                            .serialNumber(buildTeacherSerial(index))
                            .firstName(firstName)
                            .lastName(lastName)
                            .dateOfBirth(LocalDate.of(1988 + index, Math.min(index + 1, 12), Math.min(index + 5, 28)))
                            .phoneNumber("05010000" + String.format("%02d", index))
                            .address("Baku")
                            .faculty(faculty)
                            .hoursTaught(hoursTaught)
                            .build();

                    teacher.setTeacherProfile(teacherProfile);
                    teacherProfile.setUser(teacher);

                    return userRepository.save(teacher);
                });
    }

    private Specialization ensureSpecialization(Faculty faculty, User primaryTeacher) {
        return specializationRepository.findAllByOrderByNameAsc()
                .stream()
                .filter(specialization ->
                        "Computer Science".equalsIgnoreCase(specialization.getName())
                                && specialization.getFaculty().getId().equals(faculty.getId()))
                .findFirst()
                .orElseGet(() -> specializationRepository.save(Specialization.builder()
                        .name("Computer Science")
                        .faculty(faculty)
                        .assignedTeacher(primaryTeacher.getTeacherProfile())
                        .semesterCreditTarget(30)
                        .archived(false)
                        .build()));
    }

    private List<StudentGroup> ensureStudentGroups(Specialization specialization, List<User> teachers) {
        return List.of(
                ensureStudentGroup("6525a1", 1, specialization, teachers),
                ensureStudentGroup("6525a2", 1, specialization, teachers),
                ensureStudentGroup("6525a3", 1, specialization, teachers),
                ensureStudentGroup("6525a4", 1, specialization, teachers)
        );
    }

    private StudentGroup ensureStudentGroup(
            String groupName,
            int studyYear,
            Specialization specialization,
            List<User> teachers
    ) {
        StudentGroup studentGroup = studentGroupRepository.findAllByOrderByIdDesc()
                .stream()
                .filter(group ->
                        groupName.equalsIgnoreCase(group.getName())
                                && group.getSpecialization().getId().equals(specialization.getId()))
                .findFirst()
                .orElseGet(() -> StudentGroup.builder()
                        .name(groupName)
                        .faculty(specialization.getFaculty())
                        .specialization(specialization)
                        .studyYear(studyYear)
                        .approved(true)
                        .archived(false)
                        .build());

        studentGroup.setFaculty(specialization.getFaculty());
        studentGroup.setSpecialization(specialization);
        studentGroup.setStudyYear(studyYear);
        studentGroup.setApproved(true);
        studentGroup.setArchived(false);

        Set<StudentGroupTeacherAssignment> assignments = new LinkedHashSet<>();
        for (int index = 0; index < teachers.size(); index++) {
            assignments.add(StudentGroupTeacherAssignment.builder()
                    .studentGroup(studentGroup)
                    .teacher(teachers.get(index).getTeacherProfile())
                    .subjectName(SUBJECT_NAMES[index])
                    .creditValue(SUBJECT_CREDITS[index])
                    .lectureSessionsPerWeek(SUBJECT_LECTURES[index])
                    .seminarSessionsPerWeek(SUBJECT_SEMINARS[index])
                    .labSessionsPerWeek(SUBJECT_LABS[index])
                    .build());
        }

        studentGroup.getTeacherAssignments().clear();
        studentGroup.getTeacherAssignments().addAll(assignments);

        return studentGroupRepository.save(studentGroup);
    }

    private void ensureStudentsForGroups(List<StudentGroup> studentGroups) {
        for (StudentGroup studentGroup : studentGroups) {
            for (int studentIndex = 1; studentIndex <= 20; studentIndex++) {
                ensureStudentForGroup(studentGroup, studentIndex);
            }
        }
    }

    private void ensureStudentForGroup(StudentGroup studentGroup, int studentIndex) {
        String email = "student." + studentGroup.getName() + "." + String.format("%02d", studentIndex) + "@springlms.local";

        User studentUser = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User student = User.builder()
                            .email(email)
                            .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                            .role(Role.STUDENT)
                            .state(State.ACTIVE)
                            .build();

                    StudentProfile studentProfile = StudentProfile.builder()
                            .finCode(buildStudentFin(studentGroup.getName(), studentIndex))
                            .serialNumber(buildStudentSerial(studentGroup.getName(), studentIndex))
                            .firstName("Student")
                            .lastName(studentGroup.getName().toUpperCase() + "-" + String.format("%02d", studentIndex))
                            .dateOfBirth(LocalDate.of(2006, ((studentIndex - 1) % 12) + 1, Math.min((studentIndex % 28) + 1, 28)))
                            .phoneNumber("0502" + String.format("%06d", studentIndex))
                            .address("Baku")
                            .entranceScore(450.0 + studentIndex)
                            .studyYear(studentGroup.getStudyYear())
                            .admissionYear(2025)
                            .fundingType(studentIndex % 2 == 0 ? StudentFundingType.STATE_FUNDED : StudentFundingType.PERSONAL_CREDIT)
                            .emergencyContactName("Parent " + studentIndex)
                            .emergencyContactPhone("0503" + String.format("%06d", studentIndex))
                            .emergencyContactRelation("Parent")
                            .faculty(studentGroup.getFaculty())
                            .group(studentGroup)
                            .build();

                    student.setStudentProfile(studentProfile);
                    studentProfile.setUser(student);

                    return userRepository.save(student);
                });

        if (studentUser.getStudentProfile() != null) {
            studentUser.getStudentProfile().setFaculty(studentGroup.getFaculty());
            studentUser.getStudentProfile().setGroup(studentGroup);
            studentUser.getStudentProfile().setStudyYear(studentGroup.getStudyYear());
            userRepository.save(studentUser);
        }
    }

    private String buildTeacherFin(int index) {
        return String.format("TCH%04d", index);
    }

    private String buildTeacherSerial(int index) {
        return "AZT" + String.format("%07d", index);
    }

    private String buildStudentFin(String groupName, int studentIndex) {
        String suffix = groupName.substring(Math.max(groupName.length() - 2, 0)).toUpperCase();
        return "S" + suffix + String.format("%04d", studentIndex);
    }

    private String buildStudentSerial(String groupName, int studentIndex) {
        return "AZS" + groupName.toUpperCase() + String.format("%04d", studentIndex);
    }
}
