package com.apichangeradar.diff.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DiffRequestVersions {
    @NotNull private Integer fromVersion;
    @NotNull private Integer toVersion;
}

