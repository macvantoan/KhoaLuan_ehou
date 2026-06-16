package com.phonestore.controller;

import com.phonestore.dto.*;
import com.phonestore.model.User;
import com.phonestore.repository.UserRepository;
import com.phonestore.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationManager authManager;
    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            String token = jwtUtils.generateToken(auth);

            User user = userRepo.findByUsernameOrEmail(req.getUsername(), req.getUsername())
                .orElseThrow();
            user.setLastLogin(java.time.LocalDateTime.now());
            userRepo.save(user);

            return ResponseEntity.ok(JwtResponse.builder()
                .token(token)
                .user(JwtResponse.UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .phone(user.getPhone())
                    .avatar(user.getAvatar())
                    .role(user.getRole().name())
                    .build())
                .build());
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("message", "Tên đăng nhập hoặc mật khẩu không đúng"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tên đăng nhập đã tồn tại"));
        }
        if (userRepo.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email đã được sử dụng"));
        }

        User user = User.builder()
            .username(req.getUsername())
            .email(req.getEmail())
            .password(encoder.encode(req.getPassword()))
            .fullName(req.getFullName())
            .phone(req.getPhone())
            .gender(req.getGender())
            .newsletter(Boolean.TRUE.equals(req.getNewsletter()))
            .role(User.Role.USER)
            .build();

        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", "Đăng ký thành công! Vui lòng đăng nhập."));
    }

    @PostMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestBody Map<String, String> body) {
        boolean exists = userRepo.existsByUsername(body.get("username"));
        return ResponseEntity.ok(Map.of("available", !exists));
    }
}
