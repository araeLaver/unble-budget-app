package com.unble.budget.service;

import com.unble.budget.dto.BudgetRequest;
import com.unble.budget.dto.BudgetResponse;
import com.unble.budget.entity.BudgetPlan;
import com.unble.budget.entity.Category;
import com.unble.budget.entity.Transaction;
import com.unble.budget.entity.User;
import com.unble.budget.repository.BudgetPlanRepository;
import com.unble.budget.repository.CategoryRepository;
import com.unble.budget.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class BudgetService {

    @Autowired
    private BudgetPlanRepository budgetPlanRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public List<BudgetResponse> getBudgetsByUserAndMonth(User user, LocalDate budgetMonth) {
        List<BudgetPlan> budgetPlans = budgetPlanRepository.findByUserAndBudgetMonth(user, budgetMonth);
        
        // 각 예산에 대해 실제 사용 금액 업데이트
        updateActualAmounts(user, budgetMonth, budgetPlans);
        
        return budgetPlans.stream()
                .map(BudgetResponse::new)
                .collect(Collectors.toList());
    }

    public BudgetResponse createBudget(User user, BudgetRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

        // 동일한 월, 카테고리에 대한 예산이 이미 존재하는지 확인
        if (budgetPlanRepository.findByUserAndCategoryAndBudgetMonth(
                user, category, request.getBudgetMonth()).isPresent()) {
            throw new RuntimeException("해당 월과 카테고리에 대한 예산이 이미 존재합니다.");
        }

        BudgetPlan budgetPlan = new BudgetPlan(user, category, request.getBudgetMonth(), request.getPlannedAmount());
        
        // 실제 사용 금액 계산
        BigDecimal actualAmount = calculateActualAmount(user, category, request.getBudgetMonth());
        budgetPlan.setActualAmount(actualAmount);
        
        budgetPlan = budgetPlanRepository.save(budgetPlan);
        return new BudgetResponse(budgetPlan);
    }

    public BudgetResponse updateBudget(User user, Long budgetId, BudgetRequest request) {
        BudgetPlan budgetPlan = budgetPlanRepository.findByIdAndUser(budgetId, user)
                .orElseThrow(() -> new RuntimeException("예산을 찾을 수 없습니다."));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

        budgetPlan.setCategory(category);
        budgetPlan.setBudgetMonth(request.getBudgetMonth());
        budgetPlan.setPlannedAmount(request.getPlannedAmount());
        
        // 실제 사용 금액 재계산
        BigDecimal actualAmount = calculateActualAmount(user, category, request.getBudgetMonth());
        budgetPlan.setActualAmount(actualAmount);
        
        budgetPlan = budgetPlanRepository.save(budgetPlan);
        return new BudgetResponse(budgetPlan);
    }

    public void deleteBudget(User user, Long budgetId) {
        BudgetPlan budgetPlan = budgetPlanRepository.findByIdAndUser(budgetId, user)
                .orElseThrow(() -> new RuntimeException("예산을 찾을 수 없습니다."));
        
        budgetPlanRepository.delete(budgetPlan);
    }

    public Map<String, Object> getBudgetSummary(User user, LocalDate budgetMonth) {
        List<BudgetPlan> budgetPlans = budgetPlanRepository.findByUserAndBudgetMonth(user, budgetMonth);
        updateActualAmounts(user, budgetMonth, budgetPlans);

        BigDecimal totalPlanned = budgetPlans.stream()
                .map(BudgetPlan::getPlannedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalActual = budgetPlans.stream()
                .map(BudgetPlan::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long overBudgetCount = budgetPlans.stream()
                .mapToLong(bp -> bp.isOverBudget() ? 1 : 0)
                .sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalPlanned", totalPlanned);
        summary.put("totalActual", totalActual);
        summary.put("totalRemaining", totalPlanned.subtract(totalActual));
        summary.put("overBudgetCount", overBudgetCount);
        summary.put("totalBudgets", budgetPlans.size());
        summary.put("budgetMonth", budgetMonth);

        return summary;
    }

    private void updateActualAmounts(User user, LocalDate budgetMonth, List<BudgetPlan> budgetPlans) {
        for (BudgetPlan budgetPlan : budgetPlans) {
            BigDecimal actualAmount = calculateActualAmount(user, budgetPlan.getCategory(), budgetMonth);
            budgetPlan.setActualAmount(actualAmount);
            budgetPlanRepository.save(budgetPlan);
        }
    }

    private BigDecimal calculateActualAmount(User user, Category category, LocalDate budgetMonth) {
        YearMonth yearMonth = YearMonth.from(budgetMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository
                .findByUserAndCategoryAndTransactionDateBetween(user, category, startDate, endDate);

        return transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getTransactionType().name()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}