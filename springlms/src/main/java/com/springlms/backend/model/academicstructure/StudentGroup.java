package com.springlms.backend.model.academicstructure;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "student_groups",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "faculty_id"})
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EducationLevel educationLevel;

    @Column(nullable = false)
    private Integer studyYear;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @Builder.Default
    @Column(nullable = false)
    private Boolean archived = false;
}