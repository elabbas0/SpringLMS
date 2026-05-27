package com.springlms.backend.service;

import com.springlms.backend.dto.GlobalScheduleConfigRequest;
import com.springlms.backend.dto.GlobalScheduleConfigResponse;
import com.springlms.backend.dto.ScheduleEntryResponse;
import com.springlms.backend.dto.UpdateScheduleEntryRequest;
import com.springlms.backend.model.academicstructure.StudentGroup;
import com.springlms.backend.model.academicstructure.StudentGroupTeacherAssignment;
import com.springlms.backend.model.schedule.GlobalScheduleConfig;
import com.springlms.backend.model.schedule.StudentGroupScheduleEntry;
import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.User;
import com.springlms.backend.repository.GlobalScheduleConfigRepository;
import com.springlms.backend.repository.StudentGroupRepository;
import com.springlms.backend.repository.StudentGroupScheduleEntryRepository;
import com.springlms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private static final long CONFIG_ID = 1L;
    private static final int BREAK_MINUTES = 10;
    private static final List<DayOfWeek> WEEK_DAYS = List.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
    );

    private final GlobalScheduleConfigRepository globalScheduleConfigRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final StudentGroupScheduleEntryRepository studentGroupScheduleEntryRepository;
    private final UserRepository userRepository;

    public GlobalScheduleConfigResponse getGlobalConfig() {
        return toGlobalConfigResponse(getOrCreateConfig());
    }

    public GlobalScheduleConfigResponse updateGlobalConfig(GlobalScheduleConfigRequest request) {
        GlobalScheduleConfig config = getOrCreateConfig();

        config.setAbsenceValue(requirePositiveOrZero(request.absenceValue(), "Absence value"));
        config.setLateValue(requirePositiveOrZero(request.lateValue(), "Late value"));
        config.setMinimumAssignedGroupsForAutoSchedule(
                requirePositive(
                        request.minimumAssignedGroupsForAutoSchedule(),
                        "Minimum assigned groups for auto schedule"
                )
        );
        config.setLectureLengthMinutes(requirePositive(request.lectureLengthMinutes(), "Lecture length"));
        config.setYear1StartTime(parseTime(request.year1StartTime(), "Year 1 start time"));
        config.setYear1EndTime(parseTime(request.year1EndTime(), "Year 1 end time"));
        config.setYear2StartTime(parseTime(request.year2StartTime(), "Year 2 start time"));
        config.setYear2EndTime(parseTime(request.year2EndTime(), "Year 2 end time"));
        config.setYear3StartTime(parseTime(request.year3StartTime(), "Year 3 start time"));
        config.setYear3EndTime(parseTime(request.year3EndTime(), "Year 3 end time"));
        config.setYear4StartTime(parseTime(request.year4StartTime(), "Year 4 start time"));
        config.setYear4EndTime(parseTime(request.year4EndTime(), "Year 4 end time"));

        validateWindow(config.getYear1StartTime(), config.getYear1EndTime(), "Year 1");
        validateWindow(config.getYear2StartTime(), config.getYear2EndTime(), "Year 2");
        validateWindow(config.getYear3StartTime(), config.getYear3EndTime(), "Year 3");
        validateWindow(config.getYear4StartTime(), config.getYear4EndTime(), "Year 4");

        return toGlobalConfigResponse(globalScheduleConfigRepository.save(config));
    }

    public List<ScheduleEntryResponse> listStudentGroupSchedule(Long groupId) {
        studentGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Student group not found."));

        return studentGroupScheduleEntryRepository.findByStudentGroupIdOrderByDayOfWeekAscStartTimeAsc(groupId)
                .stream()
                .map(this::toScheduleEntryResponse)
                .toList();
    }

    public List<ScheduleEntryResponse> generateStudentGroupSchedule(Long groupId) {
        StudentGroup studentGroup = studentGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Student group not found."));

        List<StudentGroupTeacherAssignment> assignments = studentGroup.getTeacherAssignments()
                .stream()
                .sorted(Comparator.comparing(StudentGroupTeacherAssignment::getSubjectName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        if (assignments.isEmpty()) {
            throw new IllegalArgumentException("Assign teachers to the student group first.");
        }

        GlobalScheduleConfig config = getOrCreateConfig();
        int totalCredits = assignments.stream()
                .mapToInt(StudentGroupTeacherAssignment::getCreditValue)
                .sum();

        if (!totalCreditsEqualsTarget(studentGroup, totalCredits)) {
            throw new IllegalArgumentException("This group does not meet the specialization credit target yet.");
        }

        List<TimeSlot> availableSlots = buildSlotsForYear(studentGroup.getStudyYear(), config);
        Set<Long> existingEntryIds = studentGroupScheduleEntryRepository.findByStudentGroupIdOrderByDayOfWeekAscStartTimeAsc(groupId)
                .stream()
                .map(StudentGroupScheduleEntry::getId)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);

        if (availableSlots.isEmpty()) {
            throw new IllegalArgumentException("No schedule slots available for this study year.");
        }

        if (assignments.size() < config.getMinimumAssignedGroupsForAutoSchedule()) {
            throw new IllegalArgumentException("This group needs more assigned teacher-subject entries before auto schedule can be generated.");
        }

        List<StudentGroupScheduleEntry> generatedEntries = new ArrayList<>();

        studentGroupScheduleEntryRepository.deleteByStudentGroupId(groupId);

        List<StudentGroupTeacherAssignment> expandedAssignments = buildDistributedAssignments(assignments);

        int slotIndex = 0;
        for (StudentGroupTeacherAssignment assignment : expandedAssignments) {
            boolean placed = false;

            while (slotIndex < availableSlots.size()) {
                TimeSlot slot = availableSlots.get(slotIndex++);

                if (hasTeacherOverlap(
                        assignment.getTeacher().getUser().getEmail(),
                        slot.dayOfWeek(),
                        slot.startTime(),
                        slot.endTime(),
                        existingEntryIds
                )) {
                    continue;
                }

                StudentGroupScheduleEntry entry = StudentGroupScheduleEntry.builder()
                        .studentGroup(studentGroup)
                        .teacherAssignment(assignment)
                        .dayOfWeek(slot.dayOfWeek())
                        .startTime(slot.startTime())
                        .endTime(slot.endTime())
                        .build();

                generatedEntries.add(entry);
                placed = true;
                break;
            }

            if (!placed) {
                throw new IllegalArgumentException("Not enough free slots to generate schedule without overlaps.");
            }
        }

        studentGroupScheduleEntryRepository.deleteByStudentGroupId(groupId);

        return generatedEntries.stream()
                .map(studentGroupScheduleEntryRepository::save)
                .sorted(Comparator.comparing(StudentGroupScheduleEntry::getDayOfWeek).thenComparing(StudentGroupScheduleEntry::getStartTime))
                .map(this::toScheduleEntryResponse)
                .toList();
    }

    public List<ScheduleEntryResponse> listTeacherSchedule(String teacherEmail) {
        findTeacherUser(teacherEmail);

        return studentGroupScheduleEntryRepository.findByTeacherAssignmentTeacherUserEmailOrderByDayOfWeekAscStartTimeAsc(teacherEmail)
                .stream()
                .map(this::toScheduleEntryResponse)
                .toList();
    }

    public ScheduleEntryResponse updateTeacherScheduleEntry(String teacherEmail, Long scheduleEntryId, UpdateScheduleEntryRequest request) {
        User teacher = findTeacherUser(teacherEmail);
        StudentGroupScheduleEntry entry = studentGroupScheduleEntryRepository.findById(scheduleEntryId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule entry not found."));

        if (!entry.getTeacherAssignment().getTeacher().getUser().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("You can only update your own lecture time.");
        }

        DayOfWeek dayOfWeek = parseDayOfWeek(request.dayOfWeek());
        LocalTime startTime = parseTime(request.startTime(), "Start time");
        GlobalScheduleConfig config = getOrCreateConfig();
        LocalTime endTime = startTime.plusMinutes(config.getLectureLengthMinutes());

        validateWithinYearWindow(entry.getStudentGroup().getStudyYear(), startTime, endTime, config);

        if (hasTeacherOverlap(teacherEmail, dayOfWeek, startTime, endTime, entry.getId())) {
            throw new IllegalArgumentException("Selected time overlaps with another lecture in your schedule.");
        }

        if (hasGroupOverlap(entry.getStudentGroup().getId(), dayOfWeek, startTime, endTime, entry.getId())) {
            throw new IllegalArgumentException("Selected time overlaps with another lecture for this student group.");
        }

        entry.setDayOfWeek(dayOfWeek);
        entry.setStartTime(startTime);
        entry.setEndTime(endTime);

        return toScheduleEntryResponse(studentGroupScheduleEntryRepository.save(entry));
    }

    private GlobalScheduleConfig getOrCreateConfig() {
        return globalScheduleConfigRepository.findById(CONFIG_ID)
                .orElseGet(() -> globalScheduleConfigRepository.save(GlobalScheduleConfig.builder()
                        .id(CONFIG_ID)
                        .absenceValue(1.0)
                        .lateValue(0.5)
                        .minimumAssignedGroupsForAutoSchedule(2)
                        .lectureLengthMinutes(80)
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

    private List<TimeSlot> buildSlotsForYear(Integer studyYear, GlobalScheduleConfig config) {
        LocalTime startTime = switch (studyYear) {
            case 1 -> config.getYear1StartTime();
            case 2 -> config.getYear2StartTime();
            case 3 -> config.getYear3StartTime();
            case 4 -> config.getYear4StartTime();
            default -> throw new IllegalArgumentException("Unsupported study year.");
        };

        LocalTime endTime = switch (studyYear) {
            case 1 -> config.getYear1EndTime();
            case 2 -> config.getYear2EndTime();
            case 3 -> config.getYear3EndTime();
            case 4 -> config.getYear4EndTime();
            default -> throw new IllegalArgumentException("Unsupported study year.");
        };

        List<TimeSlot> slots = new ArrayList<>();

        for (DayOfWeek dayOfWeek : WEEK_DAYS) {
            LocalTime current = startTime;

            while (!current.plusMinutes(config.getLectureLengthMinutes()).isAfter(endTime)) {
                LocalTime slotEnd = current.plusMinutes(config.getLectureLengthMinutes());
                slots.add(new TimeSlot(dayOfWeek, current, slotEnd));
                current = slotEnd.plusMinutes(BREAK_MINUTES);
            }
        }

        return slots;
    }

    private boolean totalCreditsEqualsTarget(StudentGroup studentGroup, int totalCredits) {
        Integer target = studentGroup.getSpecialization().getSemesterCreditTarget();
        return target != null && totalCredits == target;
    }

    private int sessionsPerWeekForCredits(Integer creditValue) {
        if (creditValue == null || creditValue < 1) {
            throw new IllegalArgumentException("Credit value must be at least 1.");
        }

        if (creditValue <= 2) return 1;
        if (creditValue <= 4) return 2;
        if (creditValue <= 6) return 3;
        if (creditValue <= 8) return 4;

        return (int) Math.ceil(creditValue / 2.0);
    }

    private List<StudentGroupTeacherAssignment> buildDistributedAssignments(List<StudentGroupTeacherAssignment> assignments) {
        Map<Long, Integer> remainingSessions = new HashMap<>();

        for (StudentGroupTeacherAssignment assignment : assignments) {
            remainingSessions.put(assignment.getId(), sessionsPerWeekForCredits(assignment.getCreditValue()));
        }

        List<StudentGroupTeacherAssignment> distributed = new ArrayList<>();
        boolean addedInRound;

        do {
            addedInRound = false;

            for (StudentGroupTeacherAssignment assignment : assignments) {
                Integer remaining = remainingSessions.getOrDefault(assignment.getId(), 0);
                if (remaining > 0) {
                    distributed.add(assignment);
                    remainingSessions.put(assignment.getId(), remaining - 1);
                    addedInRound = true;
                }
            }
        } while (addedInRound);

        return distributed;
    }

    private boolean hasTeacherOverlap(String teacherEmail, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        return hasTeacherOverlap(teacherEmail, dayOfWeek, startTime, endTime, (Long) null);
    }

    private boolean hasTeacherOverlap(String teacherEmail, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, Long ignoreEntryId) {
        Set<Long> ignoreIds = ignoreEntryId == null ? Set.of() : Set.of(ignoreEntryId);
        return hasTeacherOverlap(teacherEmail, dayOfWeek, startTime, endTime, ignoreIds);
    }

    private boolean hasTeacherOverlap(String teacherEmail, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, Set<Long> ignoreEntryIds) {
        return studentGroupScheduleEntryRepository.findByTeacherAssignmentTeacherUserEmailOrderByDayOfWeekAscStartTimeAsc(teacherEmail)
                .stream()
                .filter(entry -> !ignoreEntryIds.contains(entry.getId()))
                .anyMatch(entry -> overlaps(entry.getDayOfWeek(), entry.getStartTime(), entry.getEndTime(), dayOfWeek, startTime, endTime));
    }

    private boolean hasGroupOverlap(Long groupId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, Long ignoreEntryId) {
        return studentGroupScheduleEntryRepository.findByStudentGroupIdOrderByDayOfWeekAscStartTimeAsc(groupId)
                .stream()
                .filter(entry -> ignoreEntryId == null || !entry.getId().equals(ignoreEntryId))
                .anyMatch(entry -> overlaps(entry.getDayOfWeek(), entry.getStartTime(), entry.getEndTime(), dayOfWeek, startTime, endTime));
    }

    private boolean overlaps(
            DayOfWeek existingDay,
            LocalTime existingStart,
            LocalTime existingEnd,
            DayOfWeek requestedDay,
            LocalTime requestedStart,
            LocalTime requestedEnd
    ) {
        if (!existingDay.equals(requestedDay)) {
            return false;
        }

        return requestedStart.isBefore(existingEnd) && requestedEnd.isAfter(existingStart);
    }

    private void validateWithinYearWindow(Integer studyYear, LocalTime startTime, LocalTime endTime, GlobalScheduleConfig config) {
        LocalTime allowedStart = switch (studyYear) {
            case 1 -> config.getYear1StartTime();
            case 2 -> config.getYear2StartTime();
            case 3 -> config.getYear3StartTime();
            case 4 -> config.getYear4StartTime();
            default -> throw new IllegalArgumentException("Unsupported study year.");
        };

        LocalTime allowedEnd = switch (studyYear) {
            case 1 -> config.getYear1EndTime();
            case 2 -> config.getYear2EndTime();
            case 3 -> config.getYear3EndTime();
            case 4 -> config.getYear4EndTime();
            default -> throw new IllegalArgumentException("Unsupported study year.");
        };

        if (startTime.isBefore(allowedStart) || endTime.isAfter(allowedEnd)) {
            throw new IllegalArgumentException("Selected time is outside the allowed study year window.");
        }
    }

    private void validateWindow(LocalTime startTime, LocalTime endTime, String label) {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(label + " start time must be before end time.");
        }
    }

    private double requirePositiveOrZero(Double value, String label) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException(label + " must be zero or greater.");
        }
        return value;
    }

    private int requirePositive(Integer value, String label) {
        if (value == null || value < 1) {
            throw new IllegalArgumentException(label + " must be at least 1.");
        }
        return value;
    }

    private LocalTime parseTime(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }

        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(label + " must be in HH:mm format.");
        }
    }

    private DayOfWeek parseDayOfWeek(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Day of week is required.");
        }

        try {
            return DayOfWeek.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid day of week.");
        }
    }

    private User findTeacherUser(String teacherEmail) {
        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found."));

        if (user.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("Current user is not a teacher.");
        }

        return user;
    }

    private GlobalScheduleConfigResponse toGlobalConfigResponse(GlobalScheduleConfig config) {
        return new GlobalScheduleConfigResponse(
                config.getAbsenceValue(),
                config.getLateValue(),
                config.getMinimumAssignedGroupsForAutoSchedule(),
                config.getLectureLengthMinutes(),
                config.getYear1StartTime().toString(),
                config.getYear1EndTime().toString(),
                config.getYear2StartTime().toString(),
                config.getYear2EndTime().toString(),
                config.getYear3StartTime().toString(),
                config.getYear3EndTime().toString(),
                config.getYear4StartTime().toString(),
                config.getYear4EndTime().toString()
        );
    }

    private ScheduleEntryResponse toScheduleEntryResponse(StudentGroupScheduleEntry entry) {
        StudentGroupTeacherAssignment assignment = entry.getTeacherAssignment();
        String teacherName = assignment.getTeacher().getFirstName() + " " + assignment.getTeacher().getLastName();

        return new ScheduleEntryResponse(
                entry.getId(),
                entry.getStudentGroup().getId(),
                entry.getStudentGroup().getName(),
                entry.getStudentGroup().getStudyYear(),
                assignment.getTeacher().getUser().getId(),
                assignment.getTeacher().getUser().getEmail(),
                teacherName.trim(),
                assignment.getSubjectName(),
                entry.getDayOfWeek().name(),
                entry.getStartTime().toString(),
                entry.getEndTime().toString()
        );
    }

    private record TimeSlot(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
    }
}
