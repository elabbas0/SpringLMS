package com.springlms.backend.model.attendance;

import com.springlms.backend.model.schedule.StudentGroupScheduleEntry;
import com.springlms.backend.model.user.TeacherProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "attendance_sessions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"schedule_entry_id", "session_date"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_entry_id", nullable = false)
    private StudentGroupScheduleEntry scheduleEntry;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_teacher_id", nullable = false)
    private TeacherProfile createdByTeacher;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Builder.Default
    @Column(nullable = false)
    private Boolean submitted = false;

    private LocalDateTime submittedAt;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
