package com.springlms.backend.repository;

import com.springlms.backend.model.course.CourseAssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseAssignmentSubmissionRepository extends JpaRepository<CourseAssignmentSubmission, Long> {

    Optional<CourseAssignmentSubmission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    List<CourseAssignmentSubmission> findByAssignmentIdOrderBySubmittedAtDesc(Long assignmentId);

    List<CourseAssignmentSubmission> findByStudentIdOrderBySubmittedAtDesc(Long studentId);

    List<CourseAssignmentSubmission> findByAssignmentSectionTeacherAssignmentsTeacherUserEmailOrderBySubmittedAtDesc(String email);
}
