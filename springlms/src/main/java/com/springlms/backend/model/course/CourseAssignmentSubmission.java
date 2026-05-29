package com.springlms.backend.model.course;

import com.springlms.backend.model.user.StudentProfile;
import com.springlms.backend.model.user.TeacherProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "course_assignment_submissions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"assignment_id", "student_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseAssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private CourseAssignment assignment;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @Column(length = 4000)
    private String textContent;

    @Column(length = 255)
    private String fileName;

    @Column(length = 100)
    private String fileContentType;

    @Column(length = 500)
    private String storagePath;

    @Column(length = 1000)
    private String linkUrl;

    @Column(length = 4000)
    private String teacherFeedback;

    private Integer score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by_teacher_id")
    private TeacherProfile gradedBy;

    private LocalDateTime submittedAt;

    private LocalDateTime gradedAt;
}
