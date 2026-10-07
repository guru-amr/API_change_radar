package com.apichangeradar.auth.service;

import com.apichangeradar.auth.domain.User;
import com.apichangeradar.auth.dto.AuthRequest;
import com.apichangeradar.auth.dto.AuthResponse;
import com.apichangeradar.auth.dto.RegisterRequest;
import com.apichangeradar.auth.repository.UserRepository;
import com.apichangeradar.auth.security.JwtUtil;
import com.apichangeradar.common.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager am;
    private final JwtUtil jwtUtil;

    @Transactional
    public void register(RegisterRequest r) {
        if (repo.existsByEmail(r.getEmail())) throw new ConflictException("Email already in use");
        User u = new User();
        u.setName(r.getName());
        u.setEmail(r.getEmail());
        u.setPasswordHash(encoder.encode(r.getPassword()));
        u.setEnabled(true);
        repo.save(u);
    }

    public AuthResponse login(AuthRequest r) {
        am.authenticate(new UsernamePasswordAuthenticationToken(r.getEmail(), r.getPassword()));
        String token = jwtUtil.generate(r.getEmail());
        return new AuthResponse(token, "Bearer");
    }
}

