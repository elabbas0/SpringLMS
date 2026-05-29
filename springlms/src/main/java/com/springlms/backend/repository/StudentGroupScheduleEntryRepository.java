package com.springlms.backend.repository;

import com.springlms.backend.model.schedule.StudentGroupScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentGroupScheduleEntryRepository extends JpaRepository<StudentGroupScheduleEntry, Long> {

    List<StudentGroupScheduleEntry> findByStudentGroupIdOrderByDayOfWeekAscStartTimeAsc(Long studentGroupId);

    List<StudentGroupScheduleEntry> findByTeacherAssignmentTeacherUserEmailOrderByDayOfWeekAscStartTimeAsc(String email);

    void deleteByStudentGroupId(Long studentGroupId);
}
