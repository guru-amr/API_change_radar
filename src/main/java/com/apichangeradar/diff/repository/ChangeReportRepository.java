package com.apichangeradar.diff.repository;

import com.apichangeradar.apiregistry.domain.MonitoredApi;
import com.apichangeradar.diff.domain.ChangeReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChangeReportRepository extends JpaRepository<ChangeReport,Long> {
    List<ChangeReport> findByMonitoredApiOrderByCreatedAtDesc(MonitoredApi api);
    Optional<ChangeReport> findByIdAndMonitoredApi(Long id, MonitoredApi api);
}

