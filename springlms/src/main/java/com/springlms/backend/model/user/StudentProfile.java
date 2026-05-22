package com.springlms.backend.model.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String faculty;

    private Double gpa;

    private Double attendancePercentage;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}