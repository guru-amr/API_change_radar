package com.apichangeradar.snapshot.event;

import com.apichangeradar.diff.domain.ChangeReport;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChangeDetectedEvent {
    private Long monitoredApiId;
    private Long changeReportId;
    private ChangeReport changeReport;
}

