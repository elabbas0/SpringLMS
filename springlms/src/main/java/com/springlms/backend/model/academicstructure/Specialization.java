package com.springlms.backend.model.academicstructure;

import com.springlms.backend.model.user.TeacherProfile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "specializations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "faculty_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Specialization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_teacher_id")
    private TeacherProfile assignedTeacher;

    @Builder.Default
    @Column(nullable = false)
    private Integer semesterCreditTarget = 30;

    @Builder.Default
    @Column(nullable = false)
    private Boolean archived = false;
}
