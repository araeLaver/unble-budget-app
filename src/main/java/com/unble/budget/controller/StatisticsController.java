package com.unble.budget.controller;

import com.unble.budget.entity.User;
import com.unble.budget.repository.UserRepository;
import com.unble.budget.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyStatistics(
            Authentication authentication,
            @RequestParam(required = false) String month) {
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        LocalDate targetMonth = month != null ? 
                YearMonth.parse(month).atDay(1) : 
                YearMonth.now().atDay(1);

        Map<String, Object> statistics = statisticsService.getMonthlyStatistics(user, targetMonth);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/category")
    public ResponseEntity<Map<String, Object>> getCategoryStatistics(
            Authentication authentication,
            @RequestParam(required = false) String month) {
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        LocalDate targetMonth = month != null ? 
                YearMonth.parse(month).atDay(1) : 
                YearMonth.now().atDay(1);

        Map<String, Object> statistics = statisticsService.getCategoryStatistics(user, targetMonth);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/trend")
    public ResponseEntity<Map<String, Object>> getTrendStatistics(
            Authentication authentication,
            @RequestParam(defaultValue = "6") int months) {
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Map<String, Object> statistics = statisticsService.getTrendStatistics(user, months);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyStatistics(
            Authentication authentication,
            @RequestParam(required = false) String month) {
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        LocalDate targetMonth = month != null ? 
                YearMonth.parse(month).atDay(1) : 
                YearMonth.now().atDay(1);

        Map<String, Object> statistics = statisticsService.getDailyStatistics(user, targetMonth);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getOverallSummary(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Map<String, Object> summary = statisticsService.getOverallSummary(user);
        return ResponseEntity.ok(summary);
    }
}