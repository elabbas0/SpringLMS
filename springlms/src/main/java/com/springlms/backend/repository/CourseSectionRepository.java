package com.springlms.backend.repository;

import com.springlms.backend.model.course.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {

    List<CourseSection> findByArchivedFalseOrderBySectionCodeAsc();

    List<CourseSection> findDistinctByTeacherAssignmentsTeacherUserEmailOrderBySectionCodeAsc(String email);

    List<CourseSection> findDistinctByEnrollmentsStudentUserEmailOrderBySectionCodeAsc(String email);
}
