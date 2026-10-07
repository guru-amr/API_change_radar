package com.apichangeradar.apiregistry.service;

import com.apichangeradar.apiregistry.domain.MonitoredApi;
import com.apichangeradar.apiregistry.dto.MonitoredApiRequest;
import com.apichangeradar.apiregistry.dto.MonitoredApiResponse;
import com.apichangeradar.apiregistry.repository.MonitoredApiRepository;
import com.apichangeradar.auth.domain.User;
import com.apichangeradar.auth.security.CustomUserDetails;
import com.apichangeradar.common.exception.ConflictException;
import com.apichangeradar.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MonitoredApiService {
    private final MonitoredApiRepository repo;

    private User currentUser() {
        var ud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ud.getUser();
    }

    public MonitoredApiResponse create(MonitoredApiRequest req) {
        User u = currentUser();
        if (repo.existsByUserAndName(u, req.getName())) throw new ConflictException("API name already exists");
        MonitoredApi m = new MonitoredApi();
        m.setUser(u);
        m.setName(req.getName());
        m.setSpecUrl(req.getSpecUrl());
        m.setCheckIntervalSeconds(req.getCheckIntervalSeconds());
        m.setActive(req.getActive()!=null && req.getActive());
        repo.save(m);
        return toResp(m);
    }

    @Transactional(readOnly = true)
    public List<MonitoredApiResponse> list() {
        return repo.findByUser(currentUser()).stream().map(this::toResp).toList();
    }

    @Transactional(readOnly = true)
    public MonitoredApiResponse get(Long id) {
        MonitoredApi m = repo.findByIdAndUser(id, currentUser()).orElseThrow(()->new NotFoundException("Monitored API not found"));
        return toResp(m);
    }

    public MonitoredApiResponse update(Long id, MonitoredApiRequest req) {
        MonitoredApi m = repo.findByIdAndUser(id, currentUser()).orElseThrow(()->new NotFoundException("Monitored API not found"));
        if (!m.getName().equals(req.getName()) && repo.existsByUserAndName(currentUser(), req.getName())) {
            throw new ConflictException("API name already exists");
        }
        m.setName(req.getName());
        m.setSpecUrl(req.getSpecUrl());
        m.setCheckIntervalSeconds(req.getCheckIntervalSeconds());
        m.setActive(req.getActive()!=null ? req.getActive() : m.isActive());
        return toResp(m);
    }

    public void delete(Long id) {
        MonitoredApi m = repo.findByIdAndUser(id, currentUser()).orElseThrow(()->new NotFoundException("Monitored API not found"));
        repo.delete(m);
    }

    private MonitoredApiResponse toResp(MonitoredApi m) {
        return MonitoredApiResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .specUrl(m.getSpecUrl())
                .checkIntervalSeconds(m.getCheckIntervalSeconds())
                .active(m.isActive())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .lastCheckedAt(m.getLastCheckedAt())
                .lastChangedAt(m.getLastChangedAt())
                .build();
    }
}

