package com.apichangeradar.apiregistry.controller;

import com.apichangeradar.apiregistry.dto.MonitoredApiRequest;
import com.apichangeradar.apiregistry.dto.MonitoredApiResponse;
import com.apichangeradar.apiregistry.service.MonitoredApiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitored-apis")
@RequiredArgsConstructor
public class MonitoredApiController {
    private final MonitoredApiService svc;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MonitoredApiResponse create(@Valid @RequestBody MonitoredApiRequest req){ return svc.create(req); }

    @GetMapping
    public List<MonitoredApiResponse> list(){ return svc.list(); }

    @GetMapping("/{id}")
    public MonitoredApiResponse get(@PathVariable Long id){ return svc.get(id); }

    @PutMapping("/{id}")
    public MonitoredApiResponse update(@PathVariable Long id, @Valid @RequestBody MonitoredApiRequest req){ return svc.update(id,req); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){ svc.delete(id); }
}

