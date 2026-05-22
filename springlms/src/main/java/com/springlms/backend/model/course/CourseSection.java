package com.springlms.backend.model.course;

import com.springlms.backend.model.academicstructure.AcademicTerm;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "course_sections",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"section_code", "academic_term_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_term_id", nullable = false)
    private AcademicTerm academicTerm;

    @Column(name = "section_code", nullable = false, length = 100)
    private String sectionCode;

    @Builder.Default
    @Column(nullable = false)
    private Boolean archived = false;
}