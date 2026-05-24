package com.springlms.backend.repository;

import com.springlms.backend.model.academicstructure.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecializationRepository extends JpaRepository<Specialization, Long> {

    boolean existsByNameIgnoreCaseAndFacultyId(String name, Long facultyId);

    List<Specialization> findAllByOrderByNameAsc();

    List<Specialization> findByAssignedTeacherIdOrderByNameAsc(Long assignedTeacherId);
}
