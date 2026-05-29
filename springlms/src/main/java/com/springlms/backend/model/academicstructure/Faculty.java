package com.springlms.backend.model.academicstructure;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "faculties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Builder.Default
    @OneToMany(mappedBy = "faculty")
    private Set<StudentGroup> groups = new HashSet<>();

    @Builder.Default
    @Column(nullable = false)
    private Boolean archived = false;
}