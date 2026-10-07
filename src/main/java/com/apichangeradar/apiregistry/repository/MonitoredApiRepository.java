package com.apichangeradar.apiregistry.repository;

import com.apichangeradar.apiregistry.domain.MonitoredApi;
import com.apichangeradar.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonitoredApiRepository extends JpaRepository<MonitoredApi,Long> {
    List<MonitoredApi> findByUser(User user);
    Optional<MonitoredApi> findByIdAndUser(Long id, User user);
    boolean existsByUserAndName(User user, String name);
    List<MonitoredApi> findByActiveTrue();
}

