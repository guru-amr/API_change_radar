package com.apichangeradar.alert.dto;

import com.apichangeradar.alert.domain.AlertChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlertChannelRequest {
    @NotNull private AlertChannelType type;
    @NotBlank private String target;
    private Boolean enabled = true;
}

