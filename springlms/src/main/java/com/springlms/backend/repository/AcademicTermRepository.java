package com.springlms.backend.repository;

import com.springlms.backend.model.academicstructure.AcademicTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademicTermRepository extends JpaRepository<AcademicTerm, Long> {

    Optional<AcademicTerm> findFirstByActiveTrueAndArchivedFalseOrderByStartDateDesc();
}
