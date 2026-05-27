package com.springlms.backend.repository;

import com.springlms.backend.model.schedule.GlobalScheduleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GlobalScheduleConfigRepository extends JpaRepository<GlobalScheduleConfig, Long> {
}
