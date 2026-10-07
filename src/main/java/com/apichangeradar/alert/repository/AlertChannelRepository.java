package com.apichangeradar.alert.repository;

import com.apichangeradar.alert.domain.AlertChannel;
import com.apichangeradar.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertChannelRepository extends JpaRepository<AlertChannel,Long> {
    List<AlertChannel> findByUserAndEnabledTrue(User user);
}

