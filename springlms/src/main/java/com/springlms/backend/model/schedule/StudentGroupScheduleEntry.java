package com.springlms.backend.model.schedule;

import com.springlms.backend.model.academicstructure.StudentGroup;
import com.springlms.backend.model.academicstructure.StudentGroupTeacherAssignment;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "student_group_schedule_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentGroupScheduleEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_group_id", nullable = false)
    private StudentGroup studentGroup;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_assignment_id", nullable = false)
    private StudentGroupTeacherAssignment teacherAssignment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleSessionType sessionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;
}
