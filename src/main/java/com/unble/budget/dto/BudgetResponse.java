package com.unble.budget.dto;

import com.unble.budget.entity.BudgetPlan;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BudgetResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private LocalDate budgetMonth;
    private BigDecimal plannedAmount;
    private BigDecimal actualAmount;
    private double usagePercentage;
    private boolean overBudget;
    private BigDecimal remainingBudget;

    public BudgetResponse() {}

    public BudgetResponse(BudgetPlan budgetPlan) {
        this.id = budgetPlan.getId();
        this.categoryId = budgetPlan.getCategory().getId();
        this.categoryName = budgetPlan.getCategory().getName();
        this.categoryColor = budgetPlan.getCategory().getColor();
        this.budgetMonth = budgetPlan.getBudgetMonth();
        this.plannedAmount = budgetPlan.getPlannedAmount();
        this.actualAmount = budgetPlan.getActualAmount();
        this.usagePercentage = budgetPlan.getUsagePercentage();
        this.overBudget = budgetPlan.isOverBudget();
        this.remainingBudget = budgetPlan.getRemainingBudget();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCategoryColor() { return categoryColor; }
    public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }

    public LocalDate getBudgetMonth() { return budgetMonth; }
    public void setBudgetMonth(LocalDate budgetMonth) { this.budgetMonth = budgetMonth; }

    public BigDecimal getPlannedAmount() { return plannedAmount; }
    public void setPlannedAmount(BigDecimal plannedAmount) { this.plannedAmount = plannedAmount; }

    public BigDecimal getActualAmount() { return actualAmount; }
    public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }

    public double getUsagePercentage() { return usagePercentage; }
    public void setUsagePercentage(double usagePercentage) { this.usagePercentage = usagePercentage; }

    public boolean isOverBudget() { return overBudget; }
    public void setOverBudget(boolean overBudget) { this.overBudget = overBudget; }

    public BigDecimal getRemainingBudget() { return remainingBudget; }
    public void setRemainingBudget(BigDecimal remainingBudget) { this.remainingBudget = remainingBudget; }
}