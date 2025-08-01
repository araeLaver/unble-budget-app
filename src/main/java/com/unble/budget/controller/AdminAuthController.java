package com.unble.budget.controller;

import com.unble.budget.config.JwtTokenUtil;
import com.unble.budget.entity.User;
import com.unble.budget.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 관리자 인증 컨트롤러
 * admin 계정 전용 로그인 처리
 */
@RestController
@RequestMapping("/api/admin/auth")
@CrossOrigin(origins = "*")
public class AdminAuthController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserRepository userRepository;
    
    // 관리자 계정 이메일
    private static final String ADMIN_EMAIL = "admin@unble.com";
    
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody AdminLoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 관리자 계정 확인
            if (!ADMIN_EMAIL.equals(request.getEmail())) {
                response.put("success", false);
                response.put("message", "관리자 계정이 아닙니다");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 데이터베이스에서 admin 사용자 조회
            Optional<User> adminUserOpt = userRepository.findByEmail(ADMIN_EMAIL);
            if (adminUserOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "관리자 계정을 찾을 수 없습니다");
                return ResponseEntity.badRequest().body(response);
            }
            
            User adminUser = adminUserOpt.get();
            
            // 비밀번호 확인
            if (!passwordEncoder.matches(request.getPassword(), adminUser.getPassword())) {
                response.put("success", false);
                response.put("message", "비밀번호가 틀렸습니다");
                return ResponseEntity.badRequest().body(response);
            }
            
            // JWT 토큰 생성 (관리자 권한 포함)
            String token = jwtTokenUtil.generateToken(ADMIN_EMAIL);
            
            response.put("success", true);
            response.put("token", token);
            response.put("email", adminUser.getEmail());
            response.put("name", adminUser.getName());
            response.put("role", "ADMIN");
            response.put("message", "관리자 로그인 성공");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<?> verifyAdminToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtTokenUtil.getUsernameFromToken(token);
            
            // 관리자 계정인지 확인
            if (ADMIN_EMAIL.equals(email) && !jwtTokenUtil.isTokenExpired(token)) {
                response.put("valid", true);
                response.put("email", email);
                response.put("role", "ADMIN");
                return ResponseEntity.ok(response);
            } else {
                response.put("valid", false);
                response.put("message", "관리자 권한이 없습니다");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "토큰 검증 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 관리자 로그인 요청 DTO
    public static class AdminLoginRequest {
        private String email;
        private String password;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}