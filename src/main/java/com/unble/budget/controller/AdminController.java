package com.unble.budget.controller;

import com.unble.budget.entity.User;
import com.unble.budget.repository.UserRepository;
import com.unble.budget.repository.TransactionRepository;
import com.unble.budget.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.unble.budget.config.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    private static final String ADMIN_EMAIL = "admin@unble.com";

    public AdminController(UserRepository userRepository,
                          TransactionRepository transactionRepository,
                          CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!isValidAdminToken(authHeader)) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "관리자 권한이 필요합니다");
            return ResponseEntity.status(403).body(error);
        }
        try {
            List<User> users = userRepository.findAll();
            List<Map<String, Object>> userList = users.stream()
                .map(user -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", user.getId());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("name", user.getName());
                    userInfo.put("createdAt", user.getCreatedAt());
                    userInfo.put("transactionCount", transactionRepository.countByUser(user));
                    return userInfo;
                })
                .toList();

            return ResponseEntity.ok(userList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "사용자 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!isValidAdminToken(authHeader)) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "관리자 권한이 필요합니다");
            return ResponseEntity.status(403).body(error);
        }
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userRepository.count());
            stats.put("totalTransactions", transactionRepository.count());
            stats.put("totalCategories", categoryRepository.count());
            stats.put("defaultCategories", categoryRepository.findByIsDefaultTrueOrderByName().size());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "시스템 통계 조회 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!isValidAdminToken(authHeader)) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "관리자 권한이 필요합니다");
            return ResponseEntity.status(403).body(error);
        }
        try {
            if (!userRepository.existsById(userId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "사용자를 찾을 수 없습니다");
                return ResponseEntity.badRequest().body(error);
            }

            userRepository.deleteById(userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "사용자가 삭제되었습니다");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "사용자 삭제 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 관리자 토큰 검증
     */
    private boolean isValidAdminToken(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return false;
            }
            
            String token = authHeader.replace("Bearer ", "");
            String email = jwtTokenUtil.getUsernameFromToken(token);
            
            return ADMIN_EMAIL.equals(email) && !jwtTokenUtil.isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}