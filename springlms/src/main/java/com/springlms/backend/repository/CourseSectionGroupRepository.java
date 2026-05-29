package com.springlms.backend.repository;

import com.springlms.backend.model.course.CourseSectionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSectionGroupRepository extends JpaRepository<CourseSectionGroup, Long> {

    List<CourseSectionGroup> findBySectionId(Long sectionId);
}
