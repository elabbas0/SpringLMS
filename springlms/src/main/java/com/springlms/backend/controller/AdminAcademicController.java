package com.springlms.backend.controller;

import com.springlms.backend.dto.ChangeStudentGroupApprovalRequest;
import com.springlms.backend.dto.CreateSpecializationRequest;
import com.springlms.backend.dto.CreateStudentGroupRequest;
import com.springlms.backend.dto.GlobalScheduleConfigRequest;
import com.springlms.backend.dto.GlobalScheduleConfigResponse;
import com.springlms.backend.dto.ScheduleEntryResponse;
import com.springlms.backend.dto.SpecializationResponse;
import com.springlms.backend.dto.StudentGroupResponse;
import com.springlms.backend.service.AcademicStructureService;
import com.springlms.backend.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin - Academic", description = "Admin endpoints for specializations, groups, and global schedule settings.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/academic")
@RequiredArgsConstructor
public class AdminAcademicController {

    private final AcademicStructureService academicStructureService;
    private final ScheduleService scheduleService;

    @GetMapping("/specializations")
    @Operation(summary = "List specializations", description = "Returns all academic specializations.")
    public List<SpecializationResponse> listSpecializations() {
        return academicStructureService.listSpecializations();
    }

    @PostMapping("/specializations")
    @Operation(summary = "Create specialization", description = "Creates a new specialization and optionally assigns a teacher.")
    public SpecializationResponse createSpecialization(@RequestBody CreateSpecializationRequest request) {
        return academicStructureService.createSpecialization(request);
    }

    @GetMapping("/student-groups")
    @Operation(summary = "List student groups", description = "Returns student groups with teachers and students.")
    public List<StudentGroupResponse> listStudentGroups() {
        return academicStructureService.listStudentGroups();
    }

    @PostMapping("/student-groups")
    @Operation(summary = "Create student group", description = "Creates a new student group with teacher assignments.")
    public StudentGroupResponse createStudentGroup(@RequestBody CreateStudentGroupRequest request) {
        return academicStructureService.createStudentGroup(request);
    }

    @PatchMapping("/student-groups/{groupId}/approval")
    @Operation(summary = "Change group approval", description = "Approves or rejects a student group.")
    public StudentGroupResponse changeStudentGroupApproval(
            @PathVariable Long groupId,
            @RequestBody ChangeStudentGroupApprovalRequest request
    ) {
        return academicStructureService.changeStudentGroupApproval(groupId, request);
    }

    @GetMapping("/config")
    @Operation(summary = "Get global schedule config", description = "Returns the active schedule and attendance configuration.")
    public GlobalScheduleConfigResponse getGlobalConfig() {
        return scheduleService.getGlobalConfig();
    }

    @PutMapping("/config")
    @Operation(summary = "Update global schedule config", description = "Updates the global schedule and attendance configuration.")
    public GlobalScheduleConfigResponse updateGlobalConfig(@RequestBody GlobalScheduleConfigRequest request) {
        return scheduleService.updateGlobalConfig(request);
    }

    @GetMapping("/student-groups/{groupId}/schedule")
    @Operation(summary = "List group schedule", description = "Returns the generated schedule for a student group.")
    public List<ScheduleEntryResponse> listStudentGroupSchedule(@PathVariable Long groupId) {
        return scheduleService.listStudentGroupSchedule(groupId);
    }

    @PostMapping("/student-groups/{groupId}/schedule/generate")
    @Operation(summary = "Generate group schedule", description = "Generates or refreshes the schedule for a student group.")
    public List<ScheduleEntryResponse> generateStudentGroupSchedule(@PathVariable Long groupId) {
        return scheduleService.generateStudentGroupSchedule(groupId);
    }
}
