package com.springlms.backend.repository;

import com.springlms.backend.model.academicstructure.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {

    boolean existsByNameIgnoreCaseAndSpecializationId(String name, Long specializationId);

    List<StudentGroup> findAllByOrderByIdDesc();

    List<StudentGroup> findByCreatedByTeacherIdOrderByIdDesc(Long createdByTeacherId);
}
