package com.springlms.backend.model.academicstructure;

import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "student_groups",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "specialization_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id", nullable = false)
    private Specialization specialization;

    @Column(nullable = false)
    private Integer studyYear;

    @Builder.Default
    @Column(nullable = false)
    private Boolean approved = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean archived = false;

    @Builder.Default
    @OneToMany(mappedBy = "studentGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentGroupTeacherAssignment> teacherAssignments = new LinkedHashSet<>();
}
