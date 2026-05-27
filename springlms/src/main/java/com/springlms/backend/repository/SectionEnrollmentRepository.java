package com.springlms.backend.repository;

import com.springlms.backend.model.enrollment.SectionEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionEnrollmentRepository extends JpaRepository<SectionEnrollment, Long> {

    List<SectionEnrollment> findBySectionId(Long sectionId);

    List<SectionEnrollment> findByStudentId(Long studentId);

    Optional<SectionEnrollment> findByStudentIdAndSectionId(Long studentId, Long sectionId);
}
