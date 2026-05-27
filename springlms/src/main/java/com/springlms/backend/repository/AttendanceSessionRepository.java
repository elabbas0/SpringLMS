package com.springlms.backend.repository;

import com.springlms.backend.model.attendance.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    Optional<AttendanceSession> findByScheduleEntryIdAndSessionDate(Long scheduleEntryId, LocalDate sessionDate);

    List<AttendanceSession> findByScheduleEntryStudentGroupId(Long studentGroupId);

    List<AttendanceSession> findByScheduleEntryStudentGroupIdAndSubmittedTrue(Long studentGroupId);
}
