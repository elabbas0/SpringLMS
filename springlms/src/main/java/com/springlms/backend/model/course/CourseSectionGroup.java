package com.springlms.backend.model.course;

import com.springlms.backend.model.academicstructure.StudentGroup;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "course_section_groups",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"section_id", "group_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSectionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private StudentGroup group;
}