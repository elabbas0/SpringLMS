package com.springlms.backend.repository;

import com.springlms.backend.model.course.TeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long> {

    List<TeacherAssignment> findBySectionId(Long sectionId);

    List<TeacherAssignment> findByTeacherUserEmailOrderByIdAsc(String email);
}
