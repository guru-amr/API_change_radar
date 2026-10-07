package com.apichangeradar.apiregistry.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MonitoredApiResponse {
    private Long id;
    private String name;
    private String specUrl;
    private Integer checkIntervalSeconds;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastCheckedAt;
    private Instant lastChangedAt;
}

