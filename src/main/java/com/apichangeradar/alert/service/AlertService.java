package com.apichangeradar.alert.service;

import com.apichangeradar.alert.domain.*;
import com.apichangeradar.alert.dto.AlertChannelRequest;
import com.apichangeradar.alert.dto.AlertChannelResponse;
import com.apichangeradar.alert.repository.AlertChannelRepository;
import com.apichangeradar.alert.repository.AlertLogRepository;
import com.apichangeradar.apiregistry.domain.MonitoredApi;
import com.apichangeradar.apiregistry.repository.MonitoredApiRepository;
import com.apichangeradar.auth.domain.User;
import com.apichangeradar.auth.security.CustomUserDetails;
import com.apichangeradar.common.exception.NotFoundException;
import com.apichangeradar.diff.domain.ChangeReport;
import com.apichangeradar.diff.repository.ChangeReportRepository;
import com.apichangeradar.snapshot.event.ChangeDetectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class AlertService {
    private final MonitoredApiRepository apiRepo;
    private final ChangeReportRepository reportRepo;
    private final AlertChannelRepository chRepo;
    private final AlertLogRepository logRepo;
    private final SlackWebhookSender slackSender;
    private final CustomWebhookSender customSender;

    @Autowired(required = false)
    private EmailAlertSender emailSender;

    public AlertService(MonitoredApiRepository apiRepo, ChangeReportRepository reportRepo,
                        AlertChannelRepository chRepo, AlertLogRepository logRepo,
                        SlackWebhookSender slackSender, CustomWebhookSender customSender) {
        this.apiRepo = apiRepo;
        this.reportRepo = reportRepo;
        this.chRepo = chRepo;
        this.logRepo = logRepo;
        this.slackSender = slackSender;
        this.customSender = customSender;
    }

    @Async("alertTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChangeDetected(ChangeDetectedEvent ev) {
        MonitoredApi api = apiRepo.findById(ev.getMonitoredApiId()).orElse(null);
        if (api == null) return;
        ChangeReport report = ev.getChangeReport() != null
                ? ev.getChangeReport()
                : reportRepo.findById(ev.getChangeReportId()).orElse(null);
        if (report == null) return;
        User user = api.getUser();
        List<AlertChannel> channels = chRepo.findByUserAndEnabledTrue(user);
        if (channels.isEmpty()) return;
        String summary = buildSummary(api, report);
        for (AlertChannel ch : channels) {
            AlertLog al = new AlertLog();
            al.setChangeReport(report);
            al.setChannelType(ch.getType());
            al.setTarget(ch.getTarget());
            boolean ok = switch (ch.getType()) {
                case EMAIL -> {
                    if (emailSender == null) {
                        log.warn("Email alert skipped - no mail sender configured");
                        yield false;
                    }
                    yield emailSender.send(ch.getTarget(), "API Change Alert: " + api.getName(), summary);
                }
                case SLACK_WEBHOOK -> slackSender.send(ch.getTarget(), summary);
                case CUSTOM_WEBHOOK -> customSender.send(ch.getTarget(), Map.of(
                        "monitoredApiId", api.getId(),
                        "apiName", api.getName(),
                        "reportId", report.getId(),
                        "fromVersion", report.getFromVersion(),
                        "toVersion", report.getToVersion(),
                        "breakingCount", report.getBreakingCount(),
                        "nonBreakingCount", report.getNonBreakingCount(),
                        "deprecationCount", report.getDeprecationCount(),
                        "summary", summary
                ));
            };
            al.setStatus(ok ? AlertStatus.SENT : AlertStatus.FAILED);
            logRepo.save(al);
        }
    }

    private String buildSummary(MonitoredApi api, ChangeReport r) {
        StringBuilder sb = new StringBuilder();
        sb.append("API: ").append(api.getName()).append("\n");
        sb.append("Spec: ").append(api.getSpecUrl()).append("\n");
        sb.append("Versions: ").append(r.getFromVersion()).append(" -> ").append(r.getToVersion()).append("\n");
        sb.append("Breaking: ").append(r.getBreakingCount()).append(" | Non-breaking: ").append(r.getNonBreakingCount()).append(" | Deprecation: ").append(r.getDeprecationCount()).append("\n\n");
        sb.append("Top changes:\n");
        for (var ci : r.getChangeItems().stream().limit(20).toList()) {
            sb.append("- [").append(ci.getSeverity()).append("] ").append(ci.getType()).append(" | ").append(ci.getPath()).append(" | ").append(ci.getDescription()).append("\n");
        }
        return sb.toString();
    }

    private User currentUser() {
        var ud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ud.getUser();
    }

    public AlertChannelResponse create(AlertChannelRequest req) {
        User u = currentUser();
        AlertChannel c = new AlertChannel();
        c.setUser(u);
        c.setType(req.getType());
        c.setTarget(req.getTarget());
        c.setEnabled(req.getEnabled() != null && req.getEnabled());
        chRepo.save(c);
        return toResp(c);
    }

    @Transactional(readOnly = true)
    public List<AlertChannelResponse> list() {
        return chRepo.findByUserAndEnabledTrue(currentUser()).stream().map(this::toResp).toList();
    }

    public void delete(Long id) {
        AlertChannel c = chRepo.findById(id).orElseThrow(() -> new NotFoundException("Channel not found"));
        if (!c.getUser().getId().equals(currentUser().getId())) throw new AccessDeniedException("Not your channel");
        chRepo.delete(c);
    }

    private AlertChannelResponse toResp(AlertChannel c) {
        return AlertChannelResponse.builder()
                .id(c.getId())
                .type(c.getType())
                .target(c.getTarget())
                .enabled(c.isEnabled())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}

