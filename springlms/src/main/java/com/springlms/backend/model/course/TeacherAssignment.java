package com.springlms.backend.model.course;

import com.springlms.backend.model.user.TeacherProfile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "teacher_assignments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"section_id", "assignment_type"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherProfile teacher;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false)
    private TeacherAssignmentType assignmentType;
}