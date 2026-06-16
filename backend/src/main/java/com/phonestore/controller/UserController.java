package com.phonestore.controller;

import com.phonestore.model.User;
import com.phonestore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder encoder;

    @GetMapping("/users/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        return userRepo.findByUsernameOrEmail(auth.getName(), auth.getName())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, Authentication auth) {
        User user = userRepo.findByUsernameOrEmail(auth.getName(), auth.getName()).orElseThrow();
        if (body.containsKey("fullName")) user.setFullName(body.get("fullName"));
        if (body.containsKey("phone")) user.setPhone(body.get("phone"));
        if (body.containsKey("address")) user.setAddress(body.get("address"));
        if (body.containsKey("avatar")) user.setAvatar(body.get("avatar"));
        return ResponseEntity.ok(userRepo.save(user));
    }

    @PostMapping("/users/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, Authentication auth) {
        User user = userRepo.findByUsernameOrEmail(auth.getName(), auth.getName()).orElseThrow();
        if (!encoder.matches(body.get("currentPassword"), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu hiện tại không đúng"));
        }
        if (body.get("newPassword").length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu mới phải có ít nhất 8 ký tự"));
        }
        user.setPassword(encoder.encode(body.get("newPassword")));
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", "Đã đổi mật khẩu thành công"));
    }

    // ADMIN: Get all users
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(@RequestParam(required = false) String q,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userRepo.search(q, pageable);
        return ResponseEntity.ok(Map.of(
            "content", users.getContent(),
            "totalElements", users.getTotalElements(),
            "totalPages", users.getTotalPages()
        ));
    }

    // ADMIN: Toggle user active
    @PatchMapping("/admin/users/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUser(@PathVariable Long id) {
        return userRepo.findById(id).map(user -> {
            user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
            return ResponseEntity.ok(userRepo.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ADMIN: Change user role
    @PatchMapping("/admin/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return userRepo.findById(id).map(user -> {
            user.setRole(User.Role.valueOf(body.get("role")));
            return ResponseEntity.ok(userRepo.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }
}
