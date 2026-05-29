package com.springlms.backend.model.user;

import com.springlms.backend.model.academicstructure.Faculty;
import com.springlms.backend.model.user.StudentFundingType;
import com.springlms.backend.model.academicstructure.StudentGroup;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    @Column(nullable = false, unique = true, length = 7)
    private String finCode;

    @Column(nullable = false, unique = true, length = 20)
    private String serialNumber;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 30)
    private String phoneNumber;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false)
    private Double entranceScore;

    @Column(nullable = false)
    private Integer studyYear;

    @Column(nullable = false)
    private Integer admissionYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StudentFundingType fundingType;

    @Column(length = 150)
    private String emergencyContactName;

    @Column(length = 30)
    private String emergencyContactPhone;

    @Column(length = 100)
    private String emergencyContactRelation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private StudentGroup group;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
