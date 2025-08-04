package com.unble.budget.repository;

import com.unble.budget.entity.Category;
import com.unble.budget.entity.Transaction;
import com.unble.budget.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);
    
    List<Transaction> findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(
            User user, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user " +
           "AND t.transactionType = 'EXPENSE' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalExpenseForPeriod(@Param("user") User user, 
                                       @Param("startDate") LocalDate startDate, 
                                       @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user " +
           "AND t.transactionType = 'INCOME' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalIncomeForPeriod(@Param("user") User user, 
                                      @Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);
    
    long countByUser(User user);
    
    // 카테고리별 통계
    @Query("SELECT c.name, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "RIGHT JOIN Category c ON t.category = c " +
           "WHERE (t.user = :user OR t.user IS NULL) AND c.categoryType = :categoryType " +
           "AND (t.transactionDate IS NULL OR t.transactionDate BETWEEN :startDate AND :endDate) " +
           "GROUP BY c.name ORDER BY SUM(t.amount) DESC")
    List<Object[]> getCategoryStatistics(@Param("user") User user, 
                                         @Param("categoryType") Category.CategoryType categoryType,
                                         @Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
    
    // 월별 통계
    @Query("SELECT EXTRACT(YEAR FROM t.transactionDate), EXTRACT(MONTH FROM t.transactionDate), " +
           "t.transactionType, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user GROUP BY EXTRACT(YEAR FROM t.transactionDate), " +
           "EXTRACT(MONTH FROM t.transactionDate), t.transactionType " +
           "ORDER BY EXTRACT(YEAR FROM t.transactionDate) DESC, EXTRACT(MONTH FROM t.transactionDate) DESC")
    List<Object[]> getMonthlyStatistics(@Param("user") User user);
    
    // 차트용 데이터 조회 메서드들 - 네이티브 쿼리로 변경
    @Query(value = "SELECT " +
           "CONCAT(EXTRACT(YEAR FROM t.transaction_date), '-', " +
           "LPAD(CAST(EXTRACT(MONTH FROM t.transaction_date) AS TEXT), 2, '0')) as month, " +
           "t.transaction_type as type, " +
           "COALESCE(SUM(t.amount), 0) as amount " +
           "FROM unble_dev.transactions t " +
           "WHERE t.user_id = :userId AND t.transaction_date BETWEEN :startDate AND :endDate " +
           "GROUP BY EXTRACT(YEAR FROM t.transaction_date), EXTRACT(MONTH FROM t.transaction_date), t.transaction_type " +
           "ORDER BY EXTRACT(YEAR FROM t.transaction_date), EXTRACT(MONTH FROM t.transaction_date)", 
           nativeQuery = true)
    List<Object[]> findMonthlyTransactionData(@Param("userId") Long userId, 
                                             @Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);
    
    @Query(value = "SELECT " +
           "COALESCE(c.name, '미분류') as category, " +
           "COALESCE(c.color, '#666666') as color, " +
           "COALESCE(SUM(t.amount), 0) as amount " +
           "FROM unble_dev.transactions t " +
           "LEFT JOIN unble_dev.categories c ON t.category_id = c.id " +
           "WHERE t.user_id = :userId AND t.transaction_type = 'EXPENSE' " +
           "AND t.transaction_date BETWEEN :startDate AND :endDate " +
           "GROUP BY c.name, c.color " +
           "ORDER BY SUM(t.amount) DESC", 
           nativeQuery = true)
    List<Object[]> findCategoryExpenseData(@Param("userId") Long userId, 
                                          @Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    @Query(value = "SELECT " +
           "t.transaction_date as date, " +
           "COALESCE(SUM(CASE WHEN t.transaction_type = 'INCOME' THEN t.amount ELSE 0 END), 0) as income, " +
           "COALESCE(SUM(CASE WHEN t.transaction_type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as expense " +
           "FROM unble_dev.transactions t " +
           "WHERE t.user_id = :userId AND t.transaction_date BETWEEN :startDate AND :endDate " +
           "GROUP BY t.transaction_date " +
           "ORDER BY t.transaction_date", 
           nativeQuery = true)
    List<Object[]> findDailyTransactionData(@Param("userId") Long userId, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    // 추가 메서드들
    List<Transaction> findByUser(User user);
    
    List<Transaction> findByUserAndTransactionDateBetween(User user, LocalDate startDate, LocalDate endDate);
    
    List<Transaction> findByUserAndCategoryAndTransactionDateBetween(User user, Category category, 
                                                                    LocalDate startDate, LocalDate endDate);
}