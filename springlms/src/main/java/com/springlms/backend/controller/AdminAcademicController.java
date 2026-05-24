package com.springlms.backend.controller;

import com.springlms.backend.dto.ChangeStudentGroupApprovalRequest;
import com.springlms.backend.dto.CreateSpecializationRequest;
import com.springlms.backend.dto.SpecializationResponse;
import com.springlms.backend.dto.StudentGroupResponse;
import com.springlms.backend.service.AcademicStructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/academic")
@RequiredArgsConstructor
public class AdminAcademicController {

    private final AcademicStructureService academicStructureService;

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

    @PatchMapping("/student-groups/{groupId}/approval")
    public StudentGroupResponse changeStudentGroupApproval(
            @PathVariable Long groupId,
            @RequestBody ChangeStudentGroupApprovalRequest request
    ) {
        return academicStructureService.changeStudentGroupApproval(groupId, request);
    }
}
