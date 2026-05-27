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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/academic")
@RequiredArgsConstructor
public class AdminAcademicController {

    private final AcademicStructureService academicStructureService;
    private final ScheduleService scheduleService;

    @GetMapping("/specializations")
    public List<SpecializationResponse> listSpecializations() {
        return academicStructureService.listSpecializations();
    }

    @PostMapping("/specializations")
    public SpecializationResponse createSpecialization(@RequestBody CreateSpecializationRequest request) {
        return academicStructureService.createSpecialization(request);
    }

    @GetMapping("/student-groups")
    public List<StudentGroupResponse> listStudentGroups() {
        return academicStructureService.listStudentGroups();
    }

    @PostMapping("/student-groups")
    public StudentGroupResponse createStudentGroup(@RequestBody CreateStudentGroupRequest request) {
        return academicStructureService.createStudentGroup(request);
    }

    @PatchMapping("/student-groups/{groupId}/approval")
    public StudentGroupResponse changeStudentGroupApproval(
            @PathVariable Long groupId,
            @RequestBody ChangeStudentGroupApprovalRequest request
    ) {
        return academicStructureService.changeStudentGroupApproval(groupId, request);
    }

    @GetMapping("/config")
    public GlobalScheduleConfigResponse getGlobalConfig() {
        return scheduleService.getGlobalConfig();
    }

    @PutMapping("/config")
    public GlobalScheduleConfigResponse updateGlobalConfig(@RequestBody GlobalScheduleConfigRequest request) {
        return scheduleService.updateGlobalConfig(request);
    }

    @GetMapping("/student-groups/{groupId}/schedule")
    public List<ScheduleEntryResponse> listStudentGroupSchedule(@PathVariable Long groupId) {
        return scheduleService.listStudentGroupSchedule(groupId);
    }

    @PostMapping("/student-groups/{groupId}/schedule/generate")
    public List<ScheduleEntryResponse> generateStudentGroupSchedule(@PathVariable Long groupId) {
        return scheduleService.generateStudentGroupSchedule(groupId);
    }
}
