package com.croplink.controller;

import com.croplink.dto.ApiResponse;
import com.croplink.entity.User;
import com.croplink.exception.AppException;
import com.croplink.repository.UserRepository;
import com.croplink.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile(@AuthenticationPrincipal CustomUserDetails user) {
        User u = userRepository.findById(user.getId()).orElseThrow(() -> new AppException("User not found", 404));
        u.setPassword(null); // never expose password
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", u));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> body) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow(() -> new AppException("User not found", 404));
        if (body.containsKey("fullName") && body.get("fullName") != null) user.setFullName(body.get("fullName"));
        if (body.containsKey("phone") && body.get("phone") != null) user.setPhone(body.get("phone"));
        user = userRepository.save(user);
        user.setPassword(null);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", user));
    }
}
