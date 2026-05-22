package com.springlms.backend.model.schedule;

import com.springlms.backend.model.course.CourseSection;
import com.springlms.backend.model.course.TeacherAssignment;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_assignment_id")
    private TeacherAssignment teacherAssignment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(length = 100)
    private String room;

    @Builder.Default
    @Column(nullable = false)
    private Boolean archived = false;
}