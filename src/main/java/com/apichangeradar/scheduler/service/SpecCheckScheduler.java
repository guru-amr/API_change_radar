package com.apichangeradar.scheduler.service;

import com.apichangeradar.apiregistry.domain.MonitoredApi;
import com.apichangeradar.apiregistry.repository.MonitoredApiRepository;
import com.apichangeradar.snapshot.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name="app.scheduler.enabled", havingValue="true", matchIfMissing=true)
public class SpecCheckScheduler {
    private final MonitoredApiRepository apiRepo;
    private final SnapshotService snapshotService;

    @Scheduled(fixedDelayString = "${app.scheduler.poll-interval-ms:30000}")
    public void checkAll() {
        List<MonitoredApi> active = apiRepo.findByActiveTrue();
        Instant now = Instant.now();
        for (MonitoredApi m : active) {
            if (isDue(m, now)) {
                snapshotService.checkApi(m);
            }
        }
    }

    private boolean isDue(MonitoredApi api, Instant now) {
        if (api.getLastCheckedAt() == null) return true;
        return now.isAfter(api.getLastCheckedAt().plusSeconds(api.getCheckIntervalSeconds()));
    }
}

