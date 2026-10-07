package com.apichangeradar.snapshot.service;

import com.apichangeradar.apiregistry.domain.MonitoredApi;
import com.apichangeradar.apiregistry.repository.MonitoredApiRepository;
import com.apichangeradar.common.util.HashUtil;
import com.apichangeradar.diff.domain.ChangeReport;
import com.apichangeradar.diff.service.DiffService;
import com.apichangeradar.snapshot.domain.SpecSnapshot;
import com.apichangeradar.snapshot.event.ChangeDetectedEvent;
import com.apichangeradar.snapshot.repository.SpecSnapshotRepository;
import com.apichangeradar.snapshot.util.SpecNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SnapshotService {
    private final MonitoredApiRepository apiRepo;
    private final SpecSnapshotRepository snapRepo;
    private final SpecFetcher fetcher;
    private final SpecNormalizer normalizer;
    private final DiffService diffService;
    private final ApplicationEventPublisher publisher;

    public void checkApi(MonitoredApi api) {
        try {
            api.setLastCheckedAt(Instant.now());
            String raw = fetcher.fetch(api.getSpecUrl());
            String norm = normalizer.normalize(raw);
            String hash = HashUtil.sha256(norm);

            Optional<SpecSnapshot> prevOpt = snapRepo.findTopByMonitoredApiOrderByVersionDesc(api);
            if (prevOpt.isPresent() && prevOpt.get().getContentHash().equals(hash)) {
                log.debug("No change for {} hash={}", api.getName(), hash);
                apiRepo.save(api);
                return;
            }

            int nextVer = snapRepo.maxVersion(api)+1;
            SpecSnapshot snap = new SpecSnapshot();
            snap.setMonitoredApi(api);
            snap.setVersion(nextVer);
            snap.setContent(norm);
            snap.setContentHash(hash);
            snapRepo.save(snap);

            if (prevOpt.isPresent()) {
                ChangeReport report = diffService.compare(prevOpt.get(), snap);
                if (report != null) {
                    report.setMonitoredApi(api);
                    diffService.saveReport(report);
                    api.setLastChangedAt(Instant.now());
                    publisher.publishEvent(new ChangeDetectedEvent(api.getId(), report.getId(), report));
                }
            }
            apiRepo.save(api);
        } catch (Exception e) {
            log.error("Error checking api {}: {}", api.getId(), e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Optional<SpecSnapshot> latest(MonitoredApi api) {
        return snapRepo.findTopByMonitoredApiOrderByVersionDesc(api);
    }
}

