package com.apichangeradar.diff.dto;

import com.apichangeradar.common.domain.ChangeSeverity;
import com.apichangeradar.common.domain.ChangeType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangeItemDto {
    private Long id;
    private ChangeType type;
    private ChangeSeverity severity;
    private String path;
    private String description;
}

