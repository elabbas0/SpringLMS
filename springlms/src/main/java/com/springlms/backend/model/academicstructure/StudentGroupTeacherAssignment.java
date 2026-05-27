package com.springlms.backend.model.academicstructure;

import com.springlms.backend.model.user.TeacherProfile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "student_group_teacher_assignments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_group_id", "subject_name"}),
                @UniqueConstraint(columnNames = {"student_group_id", "teacher_id", "subject_name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentGroupTeacherAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_group_id", nullable = false)
    private StudentGroup studentGroup;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherProfile teacher;

    @Column(name = "subject_name", nullable = false, length = 150)
    private String subjectName;

    @Column(nullable = false)
    private Integer creditValue;

    @Builder.Default
    @Column(nullable = false)
    private Integer lectureSessionsPerWeek = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer seminarSessionsPerWeek = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer labSessionsPerWeek = 0;
}
