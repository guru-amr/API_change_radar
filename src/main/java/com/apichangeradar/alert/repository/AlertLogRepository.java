package com.apichangeradar.alert.repository;

import com.apichangeradar.alert.domain.AlertLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertLogRepository extends JpaRepository<AlertLog,Long> {}

