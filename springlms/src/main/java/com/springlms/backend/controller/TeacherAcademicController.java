package com.springlms.backend.controller;

import com.springlms.backend.dto.AssignStudentToGroupRequest;
import com.springlms.backend.dto.ScheduleEntryResponse;
import com.springlms.backend.dto.SpecializationResponse;
import com.springlms.backend.dto.StudentGroupResponse;
import com.springlms.backend.dto.TeacherStudentOptionResponse;
import com.springlms.backend.dto.UpdateScheduleEntryRequest;
import com.springlms.backend.service.AcademicStructureService;
import com.springlms.backend.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Teacher - Academic", description = "Teacher endpoints for groups, students, and schedules.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/teacher/academic")
@RequiredArgsConstructor
public class TeacherAcademicController {

    private final AcademicStructureService academicStructureService;
    private final ScheduleService scheduleService;

    @GetMapping("/specializations")
    @Operation(summary = "List specializations", description = "Returns the specializations assigned to the authenticated teacher.")
    public List<SpecializationResponse> listAssignedSpecializations(Authentication authentication) {
        return academicStructureService.listTeacherSpecializations(authentication.getName());
    }

    @GetMapping("/student-groups")
    @Operation(summary = "List student groups", description = "Returns the student groups assigned to the authenticated teacher.")
    public List<StudentGroupResponse> listStudentGroups(Authentication authentication) {
        return academicStructureService.listTeacherStudentGroups(authentication.getName());
    }

    @GetMapping("/students")
    @Operation(summary = "List available students", description = "Returns students that can be assigned to the teacher's groups.")
    public List<TeacherStudentOptionResponse> listAvailableStudents(Authentication authentication) {
        return academicStructureService.listTeacherAssignableStudents(authentication.getName());
    }

    @PostMapping("/student-groups/{groupId}/students")
    @Operation(summary = "Assign student to group", description = "Adds a student to one of the teacher's assigned groups.")
    public StudentGroupResponse assignStudentToGroup(
            Authentication authentication,
            @PathVariable Long groupId,
            @RequestBody AssignStudentToGroupRequest request
    ) {
        return academicStructureService.assignStudentToTeacherGroup(authentication.getName(), groupId, request);
    }

    @GetMapping("/schedule")
    @Operation(summary = "View teacher schedule", description = "Returns the authenticated teacher's schedule.")
    public List<ScheduleEntryResponse> listTeacherSchedule(Authentication authentication) {
        return scheduleService.listTeacherSchedule(authentication.getName());
    }

    @PatchMapping("/schedule/{scheduleEntryId}")
    @Operation(summary = "Update schedule entry", description = "Moves a teacher schedule entry to a different day and time.")
    public ScheduleEntryResponse updateScheduleEntry(
            Authentication authentication,
            @PathVariable Long scheduleEntryId,
            @RequestBody UpdateScheduleEntryRequest request
    ) {
        return scheduleService.updateTeacherScheduleEntry(authentication.getName(), scheduleEntryId, request);
    }
}
