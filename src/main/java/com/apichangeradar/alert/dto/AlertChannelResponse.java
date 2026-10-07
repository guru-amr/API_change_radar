package com.apichangeradar.alert.dto;

import com.apichangeradar.alert.domain.AlertChannelType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AlertChannelResponse {
    private Long id;
    private AlertChannelType type;
    private String target;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}

