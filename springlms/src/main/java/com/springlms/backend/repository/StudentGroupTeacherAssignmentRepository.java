package com.springlms.backend.repository;

import com.springlms.backend.model.academicstructure.StudentGroupTeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentGroupTeacherAssignmentRepository extends JpaRepository<StudentGroupTeacherAssignment, Long> {

    List<StudentGroupTeacherAssignment> findByTeacherUserEmailOrderByStudentGroupIdDesc(String email);
}
