package com.springlms.backend.repository;

import com.springlms.backend.model.course.CourseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, Long> {

    List<CourseAssignment> findBySectionIdOrderByDueDateAscCreatedAtDesc(Long sectionId);

    List<CourseAssignment> findBySectionTeacherAssignmentsTeacherUserEmailOrderByDueDateAscCreatedAtDesc(String email);

    List<CourseAssignment> findBySectionEnrollmentsStudentUserEmailOrderByDueDateAscCreatedAtDesc(String email);
}
