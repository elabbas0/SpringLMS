package com.springlms.backend.repository;

import com.springlms.backend.model.academicstructure.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    boolean existsByCode(String code);

    boolean existsByNameIgnoreCase(String name);

    List<Faculty> findAllByOrderByNameAsc();
}
