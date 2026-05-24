package com.springlms.backend.controller;

import com.springlms.backend.dto.AssignStudentToGroupRequest;
import com.springlms.backend.dto.CreateStudentGroupRequest;
import com.springlms.backend.dto.SpecializationResponse;
import com.springlms.backend.dto.StudentGroupResponse;
import com.springlms.backend.dto.TeacherStudentOptionResponse;
import com.springlms.backend.service.AcademicStructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/academic")
@RequiredArgsConstructor
public class TeacherAcademicController {

    private final AcademicStructureService academicStructureService;

    @GetMapping("/specializations")
    public List<SpecializationResponse> listAssignedSpecializations(Authentication authentication) {
        return academicStructureService.listTeacherSpecializations(authentication.getName());
    }

    @GetMapping("/student-groups")
    public List<StudentGroupResponse> listStudentGroups(Authentication authentication) {
        return academicStructureService.listTeacherStudentGroups(authentication.getName());
    }

    @GetMapping("/students")
    public List<TeacherStudentOptionResponse> listAvailableStudents(Authentication authentication) {
        return academicStructureService.listTeacherAssignableStudents(authentication.getName());
    }

    @PostMapping("/student-groups")
    public StudentGroupResponse createStudentGroup(
            Authentication authentication,
            @RequestBody CreateStudentGroupRequest request
    ) {
        return academicStructureService.createTeacherStudentGroup(authentication.getName(), request);
    }

    @PostMapping("/student-groups/{groupId}/students")
    public StudentGroupResponse assignStudentToGroup(
            Authentication authentication,
            @PathVariable Long groupId,
            @RequestBody AssignStudentToGroupRequest request
    ) {
        return academicStructureService.assignStudentToTeacherGroup(authentication.getName(), groupId, request);
    }
}
