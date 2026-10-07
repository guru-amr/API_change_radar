package com.apichangeradar.apiregistry.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MonitoredApiRequest {
    @NotBlank @Size(max=100) private String name;
    @NotBlank @Size(max=2048) private String specUrl;
    @NotNull @Min(30) private Integer checkIntervalSeconds;
    private Boolean active = true;
}

