package com.springlms.backend.repository;

import com.springlms.backend.model.academicstructure.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
}
