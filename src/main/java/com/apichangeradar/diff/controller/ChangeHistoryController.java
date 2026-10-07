package com.apichangeradar.diff.controller;

import com.apichangeradar.apiregistry.domain.MonitoredApi;
import com.apichangeradar.apiregistry.repository.MonitoredApiRepository;
import com.apichangeradar.auth.security.CustomUserDetails;
import com.apichangeradar.common.exception.NotFoundException;
import com.apichangeradar.diff.dto.ChangeReportDto;
import com.apichangeradar.diff.dto.DiffRequestVersions;
import com.apichangeradar.diff.service.DiffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitored-apis/{apiId}/changes")
@RequiredArgsConstructor
public class ChangeHistoryController {
    private final MonitoredApiRepository apiRepo;
    private final DiffService diffService;

    private MonitoredApi getApi(Long apiId) {
        var ud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return apiRepo.findByIdAndUser(apiId, ud.getUser()).orElseThrow(()->new NotFoundException("Monitored API not found"));
    }

    @GetMapping
    public List<ChangeReportDto> history(@PathVariable Long apiId) {
        return diffService.history(getApi(apiId));
    }

    @GetMapping("/{reportId}")
    public ChangeReportDto get(@PathVariable Long apiId, @PathVariable Long reportId) {
        return diffService.get(getApi(apiId), reportId);
    }

    @PostMapping("/diff")
    public ChangeReportDto diff(@PathVariable Long apiId, @Valid @RequestBody DiffRequestVersions req) {
        return diffService.diffBetweenVersions(getApi(apiId), req.getFromVersion(), req.getToVersion());
    }
}

