package com.springlms.backend.repository;

import com.springlms.backend.model.course.CourseAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseAttachmentRepository extends JpaRepository<CourseAttachment, Long> {

    List<CourseAttachment> findBySectionIdOrderByCreatedAtDesc(Long sectionId);

    List<CourseAttachment> findBySectionIdInOrderByCreatedAtDesc(java.util.Collection<Long> sectionIds);
}
