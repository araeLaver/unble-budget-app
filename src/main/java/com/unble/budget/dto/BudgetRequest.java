package com.unble.budget.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BudgetRequest {
    
    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;
    
    @NotNull(message = "예산 월은 필수입니다.")
    private LocalDate budgetMonth;
    
    @NotNull(message = "계획 금액은 필수입니다.")
    @Positive(message = "계획 금액은 0보다 커야 합니다.")
    private BigDecimal plannedAmount;

    public BudgetRequest() {}

    public BudgetRequest(Long categoryId, LocalDate budgetMonth, BigDecimal plannedAmount) {
        this.categoryId = categoryId;
        this.budgetMonth = budgetMonth;
        this.plannedAmount = plannedAmount;
    }

    // Getters and Setters
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public LocalDate getBudgetMonth() { return budgetMonth; }
    public void setBudgetMonth(LocalDate budgetMonth) { this.budgetMonth = budgetMonth; }

    public BigDecimal getPlannedAmount() { return plannedAmount; }
    public void setPlannedAmount(BigDecimal plannedAmount) { this.plannedAmount = plannedAmount; }
}