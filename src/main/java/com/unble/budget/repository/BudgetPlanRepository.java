package com.unble.budget.repository;

import com.unble.budget.entity.BudgetPlan;
import com.unble.budget.entity.Category;
import com.unble.budget.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetPlanRepository extends JpaRepository<BudgetPlan, Long> {
    
    List<BudgetPlan> findByUserOrderByBudgetMonthDesc(User user);
    
    List<BudgetPlan> findByUserAndBudgetMonth(User user, LocalDate budgetMonth);
    
    Optional<BudgetPlan> findByUserAndCategoryAndBudgetMonth(User user, Category category, LocalDate budgetMonth);
    
    @Query("SELECT bp FROM BudgetPlan bp WHERE bp.user = :user " +
           "AND bp.budgetMonth BETWEEN :startMonth AND :endMonth " +
           "ORDER BY bp.budgetMonth DESC, bp.category.name ASC")
    List<BudgetPlan> findByUserAndBudgetMonthBetween(@Param("user") User user,
                                                     @Param("startMonth") LocalDate startMonth,
                                                     @Param("endMonth") LocalDate endMonth);
    
    @Query("SELECT COUNT(bp) FROM BudgetPlan bp WHERE bp.user = :user AND bp.actualAmount > bp.plannedAmount")
    long countOverBudgetPlans(@Param("user") User user);
    
    @Query("SELECT COALESCE(SUM(bp.plannedAmount), 0) FROM BudgetPlan bp " +
           "WHERE bp.user = :user AND bp.budgetMonth = :budgetMonth")
    java.math.BigDecimal getTotalPlannedAmountForMonth(@Param("user") User user, 
                                                       @Param("budgetMonth") LocalDate budgetMonth);
    
    @Query("SELECT COALESCE(SUM(bp.actualAmount), 0) FROM BudgetPlan bp " +
           "WHERE bp.user = :user AND bp.budgetMonth = :budgetMonth")
    java.math.BigDecimal getTotalActualAmountForMonth(@Param("user") User user, 
                                                      @Param("budgetMonth") LocalDate budgetMonth);
    
    Optional<BudgetPlan> findByIdAndUser(Long id, User user);
}