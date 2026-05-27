package com.springlms.backend.repository;

import com.springlms.backend.model.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByArchivedFalseOrderByNameAsc();

    Optional<Course> findByCodeIgnoreCase(String code);
}
