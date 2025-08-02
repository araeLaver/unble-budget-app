package com.unble.budget.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "budget_plans", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category_id", "budget_month"}))
public class BudgetPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "budget_month", nullable = false)
    private LocalDate budgetMonth;

    @Column(name = "planned_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal plannedAmount;

    @Column(name = "actual_amount", precision = 15, scale = 2)
    private BigDecimal actualAmount = BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public BudgetPlan() {}

    public BudgetPlan(User user, Category category, LocalDate budgetMonth, BigDecimal plannedAmount) {
        this.user = user;
        this.category = category;
        this.budgetMonth = budgetMonth;
        this.plannedAmount = plannedAmount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public LocalDate getBudgetMonth() { return budgetMonth; }
    public void setBudgetMonth(LocalDate budgetMonth) { this.budgetMonth = budgetMonth; }

    public BigDecimal getPlannedAmount() { return plannedAmount; }
    public void setPlannedAmount(BigDecimal plannedAmount) { this.plannedAmount = plannedAmount; }

    public BigDecimal getActualAmount() { return actualAmount; }
    public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 예산 대비 사용률 계산
    public double getUsagePercentage() {
        if (plannedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return actualAmount.divide(plannedAmount, 4, BigDecimal.ROUND_HALF_UP)
                          .multiply(BigDecimal.valueOf(100))
                          .doubleValue();
    }

    // 예산 초과 여부
    public boolean isOverBudget() {
        return actualAmount.compareTo(plannedAmount) > 0;
    }

    // 남은 예산
    public BigDecimal getRemainingBudget() {
        return plannedAmount.subtract(actualAmount);
    }
}