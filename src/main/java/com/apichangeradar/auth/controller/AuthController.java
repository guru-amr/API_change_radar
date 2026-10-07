package com.apichangeradar.auth.controller;

import com.apichangeradar.auth.dto.AuthRequest;
import com.apichangeradar.auth.dto.AuthResponse;
import com.apichangeradar.auth.dto.RegisterRequest;
import com.apichangeradar.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService svc;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest r) { svc.register(r); }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest r) { return svc.login(r); }
}

