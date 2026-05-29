package com.springlms.backend.model.course;

import com.springlms.backend.model.user.TeacherProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseAttachment {

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
    @Column(name = "attachment_type", nullable = false, length = 30)
    private AttachmentType attachmentType;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "resource_url", nullable = false, length = 1000)
    private String resourceUrl;

    @Column(length = 1000)
    private String description;

    @Column(length = 255)
    private String fileName;

    @Column(length = 100)
    private String fileContentType;

    @Column(length = 500)
    private String storagePath;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private Boolean archived = false;
}
