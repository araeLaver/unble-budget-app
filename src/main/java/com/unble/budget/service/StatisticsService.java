package com.unble.budget.service;

import com.unble.budget.entity.Transaction;
import com.unble.budget.entity.User;
import com.unble.budget.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Map<String, Object> getMonthlyStatistics(User user, LocalDate targetMonth) {
        YearMonth yearMonth = YearMonth.from(targetMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository
                .findByUserAndTransactionDateBetween(user, startDate, endDate);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> "INCOME".equals(t.getTransactionType().name()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getTransactionType().name()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        Map<String, Object> result = new HashMap<>();
        result.put("month", targetMonth);
        result.put("totalIncome", totalIncome);
        result.put("totalExpense", totalExpense);
        result.put("balance", balance);
        result.put("transactionCount", transactions.size());
        
        return result;
    }

    public Map<String, Object> getCategoryStatistics(User user, LocalDate targetMonth) {
        YearMonth yearMonth = YearMonth.from(targetMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository
                .findByUserAndTransactionDateBetween(user, startDate, endDate);

        // 카테고리별 지출 통계
        Map<String, BigDecimal> expenseByCategory = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getTransactionType().name()))
                .collect(Collectors.groupingBy(
                    t -> t.getCategory().getName(),
                    Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        // 카테고리별 수입 통계
        Map<String, BigDecimal> incomeByCategory = transactions.stream()
                .filter(t -> "INCOME".equals(t.getTransactionType().name()))
                .collect(Collectors.groupingBy(
                    t -> t.getCategory().getName(),
                    Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        // 카테고리별 색상 정보 포함
        Map<String, Map<String, Object>> categoryDetails = new HashMap<>();
        
        transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory().getName()))
                .forEach((categoryName, categoryTransactions) -> {
                    Transaction firstTransaction = categoryTransactions.get(0);
                    Map<String, Object> details = new HashMap<>();
                    details.put("name", categoryName);
                    details.put("color", firstTransaction.getCategory().getColor());
                    details.put("icon", firstTransaction.getCategory().getIcon());
                    details.put("expenseAmount", expenseByCategory.getOrDefault(categoryName, BigDecimal.ZERO));
                    details.put("incomeAmount", incomeByCategory.getOrDefault(categoryName, BigDecimal.ZERO));
                    details.put("transactionCount", categoryTransactions.size());
                    categoryDetails.put(categoryName, details);
                });

        Map<String, Object> result = new HashMap<>();
        result.put("month", targetMonth);
        result.put("expenseByCategory", expenseByCategory);
        result.put("incomeByCategory", incomeByCategory);
        result.put("categoryDetails", categoryDetails);
        
        return result;
    }

    public Map<String, Object> getTrendStatistics(User user, int months) {
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        
        for (int i = months - 1; i >= 0; i--) {
            YearMonth targetMonth = YearMonth.now().minusMonths(i);
            LocalDate startDate = targetMonth.atDay(1);
            LocalDate endDate = targetMonth.atEndOfMonth();

            List<Transaction> transactions = transactionRepository
                    .findByUserAndTransactionDateBetween(user, startDate, endDate);

            BigDecimal totalIncome = transactions.stream()
                    .filter(t -> "INCOME".equals(t.getTransactionType().name()))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalExpense = transactions.stream()
                    .filter(t -> "EXPENSE".equals(t.getTransactionType().name()))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", targetMonth.toString());
            monthData.put("income", totalIncome);
            monthData.put("expense", totalExpense);
            monthData.put("balance", totalIncome.subtract(totalExpense));
            
            monthlyData.add(monthData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("period", months + "개월");
        result.put("monthlyData", monthlyData);
        
        return result;
    }

    public Map<String, Object> getDailyStatistics(User user, LocalDate targetMonth) {
        YearMonth yearMonth = YearMonth.from(targetMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository
                .findByUserAndTransactionDateBetween(user, startDate, endDate);

        // 일별 지출/수입 집계
        Map<LocalDate, BigDecimal> dailyExpense = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getTransactionType().name()))
                .collect(Collectors.groupingBy(
                    Transaction::getTransactionDate,
                    Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        Map<LocalDate, BigDecimal> dailyIncome = transactions.stream()
                .filter(t -> "INCOME".equals(t.getTransactionType().name()))
                .collect(Collectors.groupingBy(
                    Transaction::getTransactionDate,
                    Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        // 일별 데이터 생성
        List<Map<String, Object>> dailyData = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date);
            dayData.put("expense", dailyExpense.getOrDefault(date, BigDecimal.ZERO));
            dayData.put("income", dailyIncome.getOrDefault(date, BigDecimal.ZERO));
            dayData.put("balance", 
                dailyIncome.getOrDefault(date, BigDecimal.ZERO)
                    .subtract(dailyExpense.getOrDefault(date, BigDecimal.ZERO)));
            dailyData.add(dayData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("month", targetMonth);
        result.put("dailyData", dailyData);
        
        return result;
    }

    public Map<String, Object> getOverallSummary(User user) {
        List<Transaction> allTransactions = transactionRepository.findByUser(user);

        BigDecimal totalIncome = allTransactions.stream()
                .filter(t -> "INCOME".equals(t.getTransactionType().name()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = allTransactions.stream()
                .filter(t -> "EXPENSE".equals(t.getTransactionType().name()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 가장 많이 사용한 카테고리
        String topExpenseCategory = allTransactions.stream()
                .filter(t -> "EXPENSE".equals(t.getTransactionType().name()))
                .collect(Collectors.groupingBy(
                    t -> t.getCategory().getName(),
                    Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("없음");

        // 최근 활동
        Optional<Transaction> lastTransaction = allTransactions.stream()
                .max(Comparator.comparing(Transaction::getCreatedAt));

        Map<String, Object> result = new HashMap<>();
        result.put("totalIncome", totalIncome);
        result.put("totalExpense", totalExpense);
        result.put("netBalance", totalIncome.subtract(totalExpense));
        result.put("totalTransactions", allTransactions.size());
        result.put("topExpenseCategory", topExpenseCategory);
        result.put("lastTransactionDate", 
            lastTransaction.map(Transaction::getTransactionDate).orElse(null));
        
        return result;
    }
}