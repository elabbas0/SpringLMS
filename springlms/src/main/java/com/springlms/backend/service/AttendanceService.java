package com.springlms.backend.service;

import com.springlms.backend.dto.AttendanceOpenRequest;
import com.springlms.backend.dto.AttendanceRecordResponse;
import com.springlms.backend.dto.AttendanceRecordUpdateRequest;
import com.springlms.backend.dto.AttendanceSaveRequest;
import com.springlms.backend.dto.AttendanceSessionResponse;
import com.springlms.backend.dto.StudentAttendanceEntryResponse;
import com.springlms.backend.dto.StudentAttendanceSummaryResponse;
import com.springlms.backend.model.academicstructure.StudentGroup;
import com.springlms.backend.model.attendance.AttendanceRecord;
import com.springlms.backend.model.attendance.AttendanceSession;
import com.springlms.backend.model.attendance.AttendanceStatus;
import com.springlms.backend.model.schedule.GlobalScheduleConfig;
import com.springlms.backend.model.schedule.ScheduleSessionType;
import com.springlms.backend.model.schedule.StudentGroupScheduleEntry;
import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.StudentProfile;
import com.springlms.backend.model.user.TeacherProfile;
import com.springlms.backend.model.user.User;
import com.springlms.backend.repository.AttendanceRecordRepository;
import com.springlms.backend.repository.AttendanceSessionRepository;
import com.springlms.backend.repository.GlobalScheduleConfigRepository;
import com.springlms.backend.repository.StudentGroupRepository;
import com.springlms.backend.repository.StudentGroupScheduleEntryRepository;
import com.springlms.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private static final long CONFIG_ID = 1L;

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final GlobalScheduleConfigRepository globalScheduleConfigRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final StudentGroupScheduleEntryRepository studentGroupScheduleEntryRepository;
    private final UserRepository userRepository;

    @Transactional
    public AttendanceSessionResponse openSession(String teacherEmail, AttendanceOpenRequest request) {
        TeacherProfile teacher = findTeacherProfileByEmail(teacherEmail);
        StudentGroupScheduleEntry scheduleEntry = studentGroupScheduleEntryRepository.findById(request.scheduleEntryId())
                .orElseThrow(() -> new IllegalArgumentException("Schedule entry not found."));

        ensureTeacherOwnsEntry(teacher, scheduleEntry);

        LocalDate sessionDate = parseDate(request.sessionDate());
        AttendanceSession session = attendanceSessionRepository.findByScheduleEntryIdAndSessionDate(scheduleEntry.getId(), sessionDate)
                .orElseGet(() -> {
                    AttendanceSession created = AttendanceSession.builder()
                            .scheduleEntry(scheduleEntry)
                            .createdByTeacher(teacher)
                            .sessionDate(sessionDate)
                            .submitted(false)
                            .build();
                    AttendanceSession saved = attendanceSessionRepository.save(created);
                    seedAbsentRecords(saved, scheduleEntry.getStudentGroup());
                    return saved;
                });

        return toAttendanceSessionResponse(session);
    }

    @Transactional
    public AttendanceSessionResponse saveSession(String teacherEmail, Long sessionId, AttendanceSaveRequest request) {
        TeacherProfile teacher = findTeacherProfileByEmail(teacherEmail);
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance session not found."));

        ensureTeacherOwnsEntry(teacher, session.getScheduleEntry());
        ensureEditable(session);

        if (request.records() != null) {
            for (AttendanceRecordUpdateRequest recordRequest : request.records()) {
                if (recordRequest == null || recordRequest.studentUserId() == null || recordRequest.status() == null) {
                    continue;
                }

                StudentProfile studentProfile = findStudentProfileByUserId(recordRequest.studentUserId());
                AttendanceRecord record = attendanceRecordRepository.findBySessionIdAndStudentId(session.getId(), studentProfile.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Attendance record not found."));
                record.setStatus(recordRequest.status());
                attendanceRecordRepository.save(record);
            }
        }

        if (Boolean.TRUE.equals(request.submitted())) {
            session.setSubmitted(true);
            if (session.getSubmittedAt() == null) {
                session.setSubmittedAt(LocalDateTime.now());
            }
        }

        return toAttendanceSessionResponse(attendanceSessionRepository.save(session));
    }

    public AttendanceSessionResponse getSession(String teacherEmail, Long sessionId) {
        TeacherProfile teacher = findTeacherProfileByEmail(teacherEmail);
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance session not found."));

        ensureTeacherOwnsEntry(teacher, session.getScheduleEntry());
        return toAttendanceSessionResponse(session);
    }

    public StudentAttendanceSummaryResponse getStudentSummary(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));

        if (student.getRole() != Role.STUDENT || student.getStudentProfile() == null) {
            throw new IllegalArgumentException("Current user is not a student.");
        }

        StudentProfile profile = student.getStudentProfile();
        StudentGroup group = profile.getGroup();
        if (group == null) {
            return new StudentAttendanceSummaryResponse(0.0, 0.0, 0, 0, 0, 0.0, getOrCreateConfig().getMinimumAttendancePercentForFinalExam(), getOrCreateConfig().getSemesterWeeks());
        }

        List<StudentGroupScheduleEntry> scheduleEntries = studentGroupScheduleEntryRepository.findByStudentGroupIdOrderByDayOfWeekAscStartTimeAsc(group.getId());
        GlobalScheduleConfig config = getOrCreateConfig();
        int totalExpectedSessions = scheduleEntries.size() * config.getSemesterWeeks();

        List<AttendanceSession> sessions = attendanceSessionRepository.findByScheduleEntryStudentGroupId(group.getId())
                .stream()
                .filter(AttendanceSession::getSubmitted)
                .toList();

        double attendedEquivalent = 0.0;
        int recordedSessions = 0;
        for (AttendanceSession session : sessions) {
            AttendanceRecord record = attendanceRecordRepository.findBySessionIdAndStudentId(session.getId(), profile.getId())
                    .orElse(null);
            if (record == null) {
                continue;
            }

            attendedEquivalent += weightForStatus(record.getStatus(), config);
            recordedSessions++;
        }

        double attendancePercentage = totalExpectedSessions == 0 ? 0.0 : (attendedEquivalent / totalExpectedSessions) * 100.0;
        double minRequiredEquivalent = totalExpectedSessions * (config.getMinimumAttendancePercentForFinalExam() / 100.0);
        double allowedAbsenceEquivalent = Math.max(0.0, totalExpectedSessions - minRequiredEquivalent);
        double usedAbsenceEquivalent = Math.max(0.0, recordedSessions - attendedEquivalent);
        double remainingAbsenceEquivalent = Math.max(0.0, allowedAbsenceEquivalent - usedAbsenceEquivalent);
        int remainingLectureCount = Math.max(0, totalExpectedSessions - recordedSessions);

        return new StudentAttendanceSummaryResponse(
                round(attendancePercentage),
                round(attendedEquivalent),
                totalExpectedSessions,
                recordedSessions,
                remainingLectureCount,
                round(remainingAbsenceEquivalent),
                config.getMinimumAttendancePercentForFinalExam(),
                config.getSemesterWeeks()
        );
    }

    public List<StudentAttendanceEntryResponse> listStudentAttendanceEntries(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));

        if (student.getRole() != Role.STUDENT || student.getStudentProfile() == null) {
            throw new IllegalArgumentException("Current user is not a student.");
        }

        StudentProfile profile = student.getStudentProfile();
        StudentGroup group = profile.getGroup();
        if (group == null) {
            return List.of();
        }

        return attendanceSessionRepository.findByScheduleEntryStudentGroupIdAndSubmittedTrue(group.getId())
                .stream()
                .sorted(Comparator
                        .comparing(AttendanceSession::getSessionDate)
                        .thenComparing(session -> session.getScheduleEntry().getStartTime()))
                .map(session -> {
                    AttendanceRecord record = attendanceRecordRepository.findBySessionIdAndStudentId(session.getId(), profile.getId())
                            .orElse(null);
                    StudentGroupScheduleEntry scheduleEntry = session.getScheduleEntry();
                    String teacherName = scheduleEntry.getTeacherAssignment().getTeacher().getFirstName()
                            + " "
                            + scheduleEntry.getTeacherAssignment().getTeacher().getLastName();

                    return new StudentAttendanceEntryResponse(
                            session.getId(),
                            scheduleEntry.getId(),
                            session.getSessionDate().toString(),
                            session.getSessionDate().getDayOfWeek().name(),
                            scheduleEntry.getTeacherAssignment().getSubjectName(),
                            scheduleEntry.getSessionType().name(),
                            teacherName.trim(),
                            record == null ? AttendanceStatus.ABSENT : record.getStatus()
                    );
                })
                .toList();
    }

    private void seedAbsentRecords(AttendanceSession session, StudentGroup studentGroup) {
        List<User> students = userRepository.findByStudentProfile_Group_IdOrderByStudentProfileFirstNameAscStudentProfileLastNameAsc(studentGroup.getId());
        for (User student : students) {
            StudentProfile profile = student.getStudentProfile();
            attendanceRecordRepository.save(AttendanceRecord.builder()
                    .session(session)
                    .student(profile)
                    .status(AttendanceStatus.ABSENT)
                    .build());
        }
    }

    private AttendanceSessionResponse toAttendanceSessionResponse(AttendanceSession session) {
        StudentGroupScheduleEntry scheduleEntry = session.getScheduleEntry();
        List<AttendanceRecordResponse> records = attendanceRecordRepository.findBySessionIdOrderByStudentFirstNameAscStudentLastNameAsc(session.getId())
                .stream()
                .map(record -> {
                    StudentProfile student = record.getStudent();
                    User user = student.getUser();
                    return new AttendanceRecordResponse(
                            user.getId(),
                            user.getEmail(),
                            student.getFirstName(),
                            student.getLastName(),
                            record.getStatus()
                    );
                })
                .toList();

        LocalDateTime submittedAt = session.getSubmittedAt();
        boolean locked = submittedAt != null && LocalDateTime.now().isAfter(submittedAt.plusHours(3));

        return new AttendanceSessionResponse(
                session.getId(),
                scheduleEntry.getId(),
                scheduleEntry.getStudentGroup().getId(),
                scheduleEntry.getStudentGroup().getName(),
                scheduleEntry.getTeacherAssignment().getSubjectName(),
                scheduleEntry.getSessionType().name(),
                scheduleEntry.getDayOfWeek().name(),
                scheduleEntry.getStartTime().toString(),
                scheduleEntry.getEndTime().toString(),
                session.getSessionDate().toString(),
                session.getSubmitted(),
                locked,
                submittedAt == null ? null : submittedAt.toString(),
                records
        );
    }

    private void ensureTeacherOwnsEntry(TeacherProfile teacher, StudentGroupScheduleEntry scheduleEntry) {
        if (!scheduleEntry.getTeacherAssignment().getTeacher().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("You can only manage attendance for your own lectures.");
        }
    }

    private void ensureEditable(AttendanceSession session) {
        LocalDateTime submittedAt = session.getSubmittedAt();
        if (submittedAt != null && LocalDateTime.now().isAfter(submittedAt.plusHours(3))) {
            throw new IllegalArgumentException("Attendance is locked after 3 hours from submission.");
        }
    }

    private TeacherProfile findTeacherProfileByEmail(String teacherEmail) {
        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found."));

        if (user.getRole() != Role.TEACHER || user.getTeacherProfile() == null) {
            throw new IllegalArgumentException("Current user is not a teacher.");
        }

        return user.getTeacherProfile();
    }

    private StudentProfile findStudentProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));

        if (user.getRole() != Role.STUDENT || user.getStudentProfile() == null) {
            throw new IllegalArgumentException("Current user is not a student.");
        }

        return user.getStudentProfile();
    }

    private GlobalScheduleConfig getOrCreateConfig() {
        return globalScheduleConfigRepository.findById(CONFIG_ID)
                .orElseGet(() -> globalScheduleConfigRepository.save(GlobalScheduleConfig.builder()
                        .id(CONFIG_ID)
                        .absenceValue(1.0)
                        .lateValue(0.5)
                        .minimumAssignedGroupsForAutoSchedule(2)
                        .lectureLengthMinutes(80)
                        .semesterWeeks(15)
                        .minimumAttendancePercentForFinalExam(70)
                        .year1StartTime(LocalTime.of(9, 0))
                        .year1EndTime(LocalTime.of(13, 20))
                        .year2StartTime(LocalTime.of(9, 0))
                        .year2EndTime(LocalTime.of(15, 0))
                        .year3StartTime(LocalTime.of(9, 0))
                        .year3EndTime(LocalTime.of(16, 30))
                        .year4StartTime(LocalTime.of(9, 0))
                        .year4EndTime(LocalTime.of(18, 0))
                        .build()));
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now();
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Session date must be in YYYY-MM-DD format.");
        }
    }

    private double weightForStatus(AttendanceStatus status, GlobalScheduleConfig config) {
        if (status == null) {
            return 0.0;
        }

        if (status == AttendanceStatus.PRESENT || status == AttendanceStatus.EXCUSED) {
            return 1.0;
        }

        if (status == AttendanceStatus.LATE) {
            double absenceValue = config.getAbsenceValue() == null || config.getAbsenceValue() <= 0 ? 1.0 : config.getAbsenceValue();
            double lateValue = config.getLateValue() == null ? 0.5 : config.getLateValue();
            return Math.max(0.0, Math.min(1.0, 1.0 - (lateValue / absenceValue)));
        }

        return 0.0;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
