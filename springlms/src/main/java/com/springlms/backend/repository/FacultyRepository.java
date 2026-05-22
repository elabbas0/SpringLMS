package com.springlms.backend.repository;

import com.springlms.backend.model.academicstructure.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    boolean existsByCode(String code);
}