package com.springlms.backend.model.course;

import com.springlms.backend.model.academicstructure.AcademicTerm;
import com.springlms.backend.model.enrollment.SectionEnrollment;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

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

    @Builder.Default
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CourseSectionGroup> groups = new LinkedHashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TeacherAssignment> teacherAssignments = new LinkedHashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CourseAttachment> attachments = new LinkedHashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SectionEnrollment> enrollments = new LinkedHashSet<>();
}
