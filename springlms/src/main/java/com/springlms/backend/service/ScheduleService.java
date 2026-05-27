package com.springlms.backend.service;

import com.springlms.backend.dto.GlobalScheduleConfigRequest;
import com.springlms.backend.dto.GlobalScheduleConfigResponse;
import com.springlms.backend.dto.ScheduleEntryResponse;
import com.springlms.backend.dto.UpdateScheduleEntryRequest;
import com.springlms.backend.model.academicstructure.StudentGroup;
import com.springlms.backend.model.academicstructure.StudentGroupTeacherAssignment;
import com.springlms.backend.model.schedule.GlobalScheduleConfig;
import com.springlms.backend.model.schedule.ScheduleSessionType;
import com.springlms.backend.model.schedule.StudentGroupScheduleEntry;
import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.User;
import com.springlms.backend.repository.GlobalScheduleConfigRepository;
import com.springlms.backend.repository.StudentGroupRepository;
import com.springlms.backend.repository.StudentGroupScheduleEntryRepository;
import com.springlms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
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

        List<PlannedSessionUnit> sessionUnits = buildSessionUnits(assignments);
        if (sessionUnits.isEmpty()) {
            throw new IllegalArgumentException("This group needs at least one weekly session.");
        }

        studentGroupScheduleEntryRepository.deleteByStudentGroupId(groupId);

        Map<DayOfWeek, Integer> dayTargets = buildDayTargets(sessionUnits.size(), studentGroup.getId());
        List<PlannedSession> plannedSessions = buildSchedulePlacements(sessionUnits, availableSlots, existingEntryIds, dayTargets);
        List<StudentGroupScheduleEntry> generatedEntries = plannedSessions.stream()
                .map(plan -> StudentGroupScheduleEntry.builder()
                        .studentGroup(studentGroup)
                        .teacherAssignment(plan.assignment())
                        .sessionType(plan.sessionType())
                        .dayOfWeek(plan.slot().dayOfWeek())
                        .startTime(plan.slot().startTime())
                        .endTime(plan.slot().endTime())
                        .build())
                .toList();

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

    public List<ScheduleEntryResponse> listStudentSchedule(String studentEmail) {
        User student = findStudentUser(studentEmail);

        if (student.getStudentProfile() == null || student.getStudentProfile().getGroup() == null) {
            return List.of();
        }

        return listStudentGroupSchedule(student.getStudentProfile().getGroup().getId());
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
        if (creditValue <= 4) return 1;
        if (creditValue <= 8) return 2;
        if (creditValue <= 10) return 3;

        return 4;
    }

    private List<PlannedSessionUnit> buildSessionUnits(List<StudentGroupTeacherAssignment> assignments) {
        List<PlannedSessionUnit> units = new ArrayList<>();

        for (StudentGroupTeacherAssignment assignment : assignments) {
            int lectureSessions = normalizeSessionCount(assignment.getLectureSessionsPerWeek());
            int seminarSessions = normalizeSessionCount(assignment.getSeminarSessionsPerWeek());
            int labSessions = normalizeSessionCount(assignment.getLabSessionsPerWeek());

            if (lectureSessions + seminarSessions + labSessions == 0) {
                int fallback = sessionsPerWeekForCredits(assignment.getCreditValue());
                int[] defaults = defaultSplitForFallback(fallback);
                lectureSessions = defaults[0];
                seminarSessions = defaults[1];
                labSessions = defaults[2];
            }

            addRepeatedUnits(units, assignment, ScheduleSessionType.LECTURE, lectureSessions);
            addRepeatedUnits(units, assignment, ScheduleSessionType.SEMINAR, seminarSessions);
            addRepeatedUnits(units, assignment, ScheduleSessionType.LABORATORY, labSessions);
        }

        return units;
    }

    private List<PlannedSession> buildSchedulePlacements(
            List<PlannedSessionUnit> assignments,
            List<TimeSlot> availableSlots,
            Set<Long> existingEntryIds,
            Map<DayOfWeek, Integer> dayTargets
    ) {
        List<PlannedSession> placements = new ArrayList<>();
        Set<String> usedSlotKeys = new HashSet<>();
        Set<Integer> usedAssignmentIndexes = new HashSet<>();
        Map<DayOfWeek, Integer> dayUsageCounts = new HashMap<>();

        if (backtrackSchedule(assignments, availableSlots, existingEntryIds, usedSlotKeys, usedAssignmentIndexes, dayUsageCounts, dayTargets, placements)) {
            return placements;
        }

        throw new IllegalArgumentException("Not enough free slots to generate schedule without overlaps.");
    }

    private boolean backtrackSchedule(
            List<PlannedSessionUnit> assignments,
            List<TimeSlot> availableSlots,
            Set<Long> existingEntryIds,
            Set<String> usedSlotKeys,
            Set<Integer> usedAssignmentIndexes,
            Map<DayOfWeek, Integer> dayUsageCounts,
            Map<DayOfWeek, Integer> dayTargets,
            List<PlannedSession> placements
    ) {
        if (usedAssignmentIndexes.size() == assignments.size()) {
            return true;
        }

        int nextAssignmentIndex = -1;
        List<TimeSlot> nextCandidates = null;

        for (int i = 0; i < assignments.size(); i++) {
            if (usedAssignmentIndexes.contains(i)) {
                continue;
            }

            PlannedSessionUnit assignment = assignments.get(i);
            List<TimeSlot> candidates = availableSlots.stream()
                    .filter(slot -> dayTargets.getOrDefault(slot.dayOfWeek(), 0) > 0)
                    .filter(slot -> dayUsageCounts.getOrDefault(slot.dayOfWeek(), 0) < dayTargets.getOrDefault(slot.dayOfWeek(), 0))
                    .filter(slot -> !usedSlotKeys.contains(slot.dayOfWeek() + ":" + slot.startTime()))
                    .filter(slot -> !hasTeacherOverlap(
                            assignment.assignment().getTeacher().getUser().getEmail(),
                            slot.dayOfWeek(),
                            slot.startTime(),
                            slot.endTime(),
                            existingEntryIds
                    ))
                    .sorted(Comparator
                            .comparingInt((TimeSlot slot) -> dayTargets.getOrDefault(slot.dayOfWeek(), 0) - dayUsageCounts.getOrDefault(slot.dayOfWeek(), 0))
                            .reversed()
                            .thenComparing(TimeSlot::dayOfWeek)
                            .thenComparing(TimeSlot::startTime))
                    .toList();

            if (candidates.isEmpty()) {
                return false;
            }

            if (nextCandidates == null || candidates.size() < nextCandidates.size()) {
                nextAssignmentIndex = i;
                nextCandidates = candidates;
                if (candidates.size() == 1) {
                    break;
                }
            }
        }

        if (nextAssignmentIndex == -1 || nextCandidates == null) {
            return false;
        }

        PlannedSessionUnit assignment = assignments.get(nextAssignmentIndex);
        usedAssignmentIndexes.add(nextAssignmentIndex);

        for (TimeSlot slot : nextCandidates) {
            String slotKey = slot.dayOfWeek() + ":" + slot.startTime();
            usedSlotKeys.add(slotKey);
            dayUsageCounts.put(slot.dayOfWeek(), dayUsageCounts.getOrDefault(slot.dayOfWeek(), 0) + 1);
            placements.add(new PlannedSession(assignment.assignment(), assignment.sessionType(), slot));

            if (backtrackSchedule(assignments, availableSlots, existingEntryIds, usedSlotKeys, usedAssignmentIndexes, dayUsageCounts, dayTargets, placements)) {
                return true;
            }

            placements.remove(placements.size() - 1);
            usedSlotKeys.remove(slotKey);
            int currentUsage = dayUsageCounts.getOrDefault(slot.dayOfWeek(), 0) - 1;
            if (currentUsage <= 0) {
                dayUsageCounts.remove(slot.dayOfWeek());
            } else {
                dayUsageCounts.put(slot.dayOfWeek(), currentUsage);
            }
        }

        usedAssignmentIndexes.remove(nextAssignmentIndex);
        return false;
    }

    private Map<DayOfWeek, Integer> buildDayTargets(int totalSessions, Long groupId) {
        int activeDays = Math.min(WEEK_DAYS.size(), Math.max(3, (int) Math.ceil(totalSessions / 3.0)));
        int basePerDay = totalSessions / activeDays;
        int remainder = totalSessions % activeDays;
        int offset = groupId == null ? 0 : Math.floorMod(groupId.intValue(), WEEK_DAYS.size());

        Map<DayOfWeek, Integer> targets = new HashMap<>();
        for (DayOfWeek day : WEEK_DAYS) {
            targets.put(day, 0);
        }

        for (int i = 0; i < activeDays; i++) {
            DayOfWeek day = WEEK_DAYS.get((i + offset) % WEEK_DAYS.size());
            targets.put(day, basePerDay + (i < remainder ? 1 : 0));
        }

        return targets;
    }

    private int normalizeSessionCount(Integer value) {
        if (value == null) {
            return 0;
        }

        if (value < 0) {
            throw new IllegalArgumentException("Session count cannot be negative.");
        }

        return value;
    }

    private int[] defaultSplitForFallback(int fallbackSessions) {
        return switch (fallbackSessions) {
            case 1 -> new int[]{1, 0, 0};
            case 2 -> new int[]{1, 1, 0};
            case 3 -> new int[]{2, 1, 0};
            case 4 -> new int[]{2, 1, 1};
            default -> new int[]{3, 1, 1};
        };
    }

    private void addRepeatedUnits(
            List<PlannedSessionUnit> units,
            StudentGroupTeacherAssignment assignment,
            ScheduleSessionType sessionType,
            int count
    ) {
        for (int i = 0; i < count; i++) {
            units.add(new PlannedSessionUnit(assignment, sessionType));
        }
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

    private User findStudentUser(String studentEmail) {
        User user = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));

        if (user.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Current user is not a student.");
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
                entry.getSessionType().name(),
                entry.getDayOfWeek().name(),
                entry.getStartTime().toString(),
                entry.getEndTime().toString()
        );
    }

    private record TimeSlot(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
    }

    private record PlannedSessionUnit(StudentGroupTeacherAssignment assignment, ScheduleSessionType sessionType) {
    }

    private record PlannedSession(StudentGroupTeacherAssignment assignment, ScheduleSessionType sessionType, TimeSlot slot) {
    }
}
