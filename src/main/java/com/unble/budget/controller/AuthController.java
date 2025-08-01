package com.unble.budget.controller;

import com.unble.budget.config.JwtTokenUtil;
import com.unble.budget.dto.AuthRequest;
import com.unble.budget.dto.AuthResponse;
import com.unble.budget.entity.User;
import com.unble.budget.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                         JwtTokenUtil jwtTokenUtil,
                         UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest authRequest) {
        try {
            if (authRequest.getName() == null || authRequest.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse("이름은 필수입니다"));
            }

            User user = userService.createUser(
                authRequest.getEmail(),
                authRequest.getPassword(),
                authRequest.getName()
            );

            String token = jwtTokenUtil.generateToken(user.getEmail());

            return ResponseEntity.ok(new AuthResponse(
                token,
                user.getEmail(),
                user.getName(),
                user.getId()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authRequest.getEmail(),
                    authRequest.getPassword()
                )
            );

            Optional<User> userOpt = userService.findByEmail(authRequest.getEmail());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse("사용자를 찾을 수 없습니다"));
            }

            User user = userOpt.get();
            String token = jwtTokenUtil.generateToken(user.getEmail());

            return ResponseEntity.ok(new AuthResponse(
                token,
                user.getEmail(),
                user.getName(),
                user.getId()
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse("이메일 또는 비밀번호가 올바르지 않습니다"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        try {
            String email = authentication.getName();
            
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse("사용자를 찾을 수 없습니다"));
            }

            User user = userOpt.get();
            return ResponseEntity.ok(new AuthResponse(
                null,
                user.getEmail(),
                user.getName(),
                user.getId()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse("토큰이 유효하지 않습니다"));
        }
    }
}