package com.apichangeradar.diff.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ChangeReportDto {
    private Long id;
    private Long monitoredApiId;
    private Long fromSnapshotId;
    private Long toSnapshotId;
    private Integer fromVersion;
    private Integer toVersion;
    private int breakingCount;
    private int nonBreakingCount;
    private int deprecationCount;
    private boolean hasBreakingChanges;
    private Instant createdAt;
    private List<ChangeItemDto> items;
}

