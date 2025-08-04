package com.unble.budget.controller;

import com.unble.budget.dto.UserSettingsRequest;
import com.unble.budget.dto.UserSettingsResponse;
import com.unble.budget.entity.User;
import com.unble.budget.entity.UserSettings;
import com.unble.budget.repository.UserRepository;
import com.unble.budget.service.UserSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user-settings")
public class UserSettingsController {

    @Autowired
    private UserSettingsService userSettingsService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<UserSettingsResponse> getUserSettings(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        UserSettingsResponse settings = userSettingsService.getUserSettings(user);
        return ResponseEntity.ok(settings);
    }

    @PutMapping
    public ResponseEntity<UserSettingsResponse> updateUserSettings(
            @Valid @RequestBody UserSettingsRequest request,
            Authentication authentication) {
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        UserSettingsResponse settings = userSettingsService.updateUserSettings(user, request);
        return ResponseEntity.ok(settings);
    }

    @PostMapping("/reset")
    public ResponseEntity<UserSettingsResponse> resetToDefault(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        UserSettingsResponse settings = userSettingsService.resetToDefault(user);
        return ResponseEntity.ok(settings);
    }
}