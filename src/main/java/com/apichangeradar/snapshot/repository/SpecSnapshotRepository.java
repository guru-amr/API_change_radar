package com.apichangeradar.snapshot.repository;

import com.apichangeradar.apiregistry.domain.MonitoredApi;
import com.apichangeradar.snapshot.domain.SpecSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SpecSnapshotRepository extends JpaRepository<SpecSnapshot,Long> {
    Optional<SpecSnapshot> findTopByMonitoredApiOrderByVersionDesc(MonitoredApi api);
    List<SpecSnapshot> findByMonitoredApiOrderByVersionAsc(MonitoredApi api);
    @Query("select coalesce(max(s.version),0) from SpecSnapshot s where s.monitoredApi = :api")
    int maxVersion(MonitoredApi api);
}

