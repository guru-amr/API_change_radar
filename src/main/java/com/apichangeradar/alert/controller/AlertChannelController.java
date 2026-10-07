package com.apichangeradar.alert.controller;

import com.apichangeradar.alert.dto.AlertChannelRequest;
import com.apichangeradar.alert.dto.AlertChannelResponse;
import com.apichangeradar.alert.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alert-channels")
@RequiredArgsConstructor
public class AlertChannelController {
    private final AlertService svc;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertChannelResponse create(@Valid @RequestBody AlertChannelRequest req){ return svc.create(req); }

    @GetMapping
    public List<AlertChannelResponse> list(){ return svc.list(); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){ svc.delete(id); }
}

