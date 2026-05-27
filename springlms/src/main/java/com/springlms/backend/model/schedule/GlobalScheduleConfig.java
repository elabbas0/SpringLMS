package com.springlms.backend.model.schedule;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "global_schedule_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalScheduleConfig {

    @Id
    private Long id;

    @Column(nullable = false)
    private Double absenceValue;

    @Column(nullable = false)
    private Double lateValue;

    @Column(nullable = false)
    private Integer minimumAssignedGroupsForAutoSchedule;

    @Column(nullable = false)
    private Integer lectureLengthMinutes;

    @Column(nullable = false)
    private LocalTime year1StartTime;

    @Column(nullable = false)
    private LocalTime year1EndTime;

    @Column(nullable = false)
    private LocalTime year2StartTime;

    @Column(nullable = false)
    private LocalTime year2EndTime;

    @Column(nullable = false)
    private LocalTime year3StartTime;

    @Column(nullable = false)
    private LocalTime year3EndTime;

    @Column(nullable = false)
    private LocalTime year4StartTime;

    @Column(nullable = false)
    private LocalTime year4EndTime;
}
