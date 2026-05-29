package com.springlms.backend.repository;

import com.springlms.backend.model.user.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, Long> {

    Optional<TeacherProfile> findByUserId(Long userId);

    Optional<TeacherProfile> findByUserEmail(String email);
}
