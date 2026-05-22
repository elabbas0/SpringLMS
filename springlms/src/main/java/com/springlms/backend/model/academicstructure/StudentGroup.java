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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @Column(nullable = false)
    private Boolean archived;
}