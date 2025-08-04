package com.unble.budget.controller;

import com.unble.budget.dto.BudgetRequest;
import com.unble.budget.dto.BudgetResponse;
import com.unble.budget.entity.BudgetPlan;
import com.unble.budget.entity.Category;
import com.unble.budget.entity.User;
import com.unble.budget.repository.BudgetPlanRepository;
import com.unble.budget.repository.CategoryRepository;
import com.unble.budget.repository.UserRepository;
import com.unble.budget.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private BudgetPlanRepository budgetPlanRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getBudgets(
            Authentication authentication,
            @RequestParam(required = false) String month) {
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        LocalDate budgetMonth = month != null ? 
                YearMonth.parse(month).atDay(1) : 
                YearMonth.now().atDay(1);

        List<BudgetResponse> budgets = budgetService.getBudgetsByUserAndMonth(user, budgetMonth);
        return ResponseEntity.ok(budgets);
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @Valid @RequestBody BudgetRequest request,
            Authentication authentication) {
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        BudgetResponse budget = budgetService.createBudget(user, request);
        return ResponseEntity.ok(budget);
    }

    @PutMapping("/{budgetId}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable Long budgetId,
            @Valid @RequestBody BudgetRequest request,
            Authentication authentication) {
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        BudgetResponse budget = budgetService.updateBudget(user, budgetId, request);
        return ResponseEntity.ok(budget);
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable Long budgetId,
            Authentication authentication) {
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        budgetService.deleteBudget(user, budgetId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getBudgetSummary(
            Authentication authentication,
            @RequestParam(required = false) String month) {
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        LocalDate budgetMonth = month != null ? 
                YearMonth.parse(month).atDay(1) : 
                YearMonth.now().atDay(1);

        Map<String, Object> summary = budgetService.getBudgetSummary(user, budgetMonth);
        return ResponseEntity.ok(summary);
    }
}