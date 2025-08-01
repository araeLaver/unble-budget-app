package com.unble.budget.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 서비스 상태 확인 컨트롤러
 * 로그인 없이도 접근 가능한 시스템 상태 정보 제공
 */
@RestController
@RequestMapping("/api/status")
public class ServiceStatusController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 시스템 기본 정보
            status.put("service", "Unble Budget App");
            status.put("version", "1.0.0");
            status.put("status", "RUNNING");
            status.put("timestamp", LocalDateTime.now());
            
            // 데이터베이스 연결 상태
            Map<String, Object> database = new HashMap<>();
            try {
                String currentSchema = jdbcTemplate.queryForObject("SELECT current_schema()", String.class);
                Integer connectionCount = jdbcTemplate.queryForObject("SELECT count(*) FROM pg_stat_activity WHERE datname = current_database()", Integer.class);
                
                database.put("status", "CONNECTED");
                database.put("schema", currentSchema);
                database.put("connections", connectionCount);
                database.put("type", "PostgreSQL");
            } catch (Exception e) {
                database.put("status", "ERROR");
                database.put("error", e.getMessage());
            }
            status.put("database", database);
            
            // 서비스 통계 (로그인 없이 확인 가능한 기본 정보)
            Map<String, Object> stats = new HashMap<>();
            try {
                Integer totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE is_active = true", Integer.class);
                Integer totalCategories = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories WHERE is_default = true", Integer.class);
                
                stats.put("active_users", totalUsers);
                stats.put("default_categories", totalCategories);
                stats.put("service_uptime", "Available");
            } catch (Exception e) {
                stats.put("error", "Unable to fetch statistics");
            }
            status.put("statistics", stats);
            
            // 환경 정보
            Map<String, Object> environment = new HashMap<>();
            environment.put("profile", System.getProperty("spring.profiles.active", "default"));
            environment.put("java_version", System.getProperty("java.version"));
            environment.put("server_port", System.getProperty("server.port", "9090"));
            status.put("environment", environment);
            
        } catch (Exception e) {
            status.put("status", "ERROR");
            status.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/")
    public ResponseEntity<Map<String, String>> getRootStatus() {
        Map<String, String> response = new HashMap<>();
        response.put("service", "Unble Budget App");
        response.put("status", "UP");
        response.put("message", "Service is running");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> getHealthCheck() {
        Map<String, String> health = new HashMap<>();
        
        try {
            // 데이터베이스 ping
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            health.put("status", "UP");
            health.put("database", "UP");
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("database", "DOWN");
            health.put("error", e.getMessage());
        }
        
        health.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }
}