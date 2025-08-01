package com.unble.budget.service;

import com.unble.budget.dto.TransactionRequest;
import com.unble.budget.dto.TransactionResponse;
import com.unble.budget.entity.Category;
import com.unble.budget.entity.Transaction;
import com.unble.budget.entity.User;
import com.unble.budget.repository.CategoryRepository;
import com.unble.budget.repository.TransactionRepository;
import com.unble.budget.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                             CategoryRepository categoryRepository,
                             UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public TransactionResponse createTransaction(String userEmail, TransactionRequest request) {
        try {
            System.out.println("거래 생성 시작 - 사용자: " + userEmail);
            System.out.println("요청 데이터: " + request.toString());
            
            // 입력 데이터 검증
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("금액은 0보다 커야 합니다");
            }
            
            if (request.getTransactionType() == null || request.getTransactionType().trim().isEmpty()) {
                throw new RuntimeException("거래 유형을 선택해주세요");
            }
            
            if (request.getTransactionDate() == null) {
                throw new RuntimeException("거래 날짜를 입력해주세요");
            }
            
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            Category category = null;
            if (request.getCategoryId() != null && request.getCategoryId() > 0) {
                category = categoryRepository.findById(request.getCategoryId())
                        .orElse(null);
                System.out.println("카테고리 조회 결과: " + (category != null ? category.getName() : "null"));
            }

            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setCategory(category);
            transaction.setAmount(request.getAmount());
            transaction.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
            
            // 거래 유형 검증 및 설정
            String transactionType = request.getTransactionType().trim().toUpperCase();
            if (!"INCOME".equals(transactionType) && !"EXPENSE".equals(transactionType)) {
                throw new RuntimeException("거래 유형은 INCOME 또는 EXPENSE여야 합니다");
            }
            
            transaction.setTransactionType(Transaction.TransactionType.valueOf(transactionType));
            transaction.setTransactionDate(request.getTransactionDate());

            System.out.println("거래 저장 시도...");
            Transaction saved = transactionRepository.save(transaction);
            System.out.println("거래 저장 완료 - ID: " + saved.getId());
            
            return new TransactionResponse(saved);
        } catch (RuntimeException e) {
            System.err.println("거래 생성 검증 오류: " + e.getMessage());
            throw e; // 검증 오류는 그대로 전달
        } catch (Exception e) {
            System.err.println("거래 생성 시스템 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("거래 생성 중 시스템 오류가 발생했습니다");
        }
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getUserTransactions(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        return transactionRepository.findByUserOrderByTransactionDateDesc(user)
                .stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getUserTransactionsByDateRange(String userEmail, 
                                                                   LocalDate startDate, 
                                                                   LocalDate endDate) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        return transactionRepository.findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(
                user, startDate, endDate)
                .stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    public TransactionResponse updateTransaction(String userEmail, Long transactionId, TransactionRequest request) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("거래를 찾을 수 없습니다"));

        if (!transaction.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("권한이 없습니다");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElse(null);
        }

        transaction.setCategory(category);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        
        try {
            transaction.setTransactionType(Transaction.TransactionType.valueOf(request.getTransactionType()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("올바르지 않은 거래 유형입니다");
        }
        
        transaction.setTransactionDate(request.getTransactionDate());

        Transaction saved = transactionRepository.save(transaction);
        return new TransactionResponse(saved);
    }

    public void deleteTransaction(String userEmail, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("거래를 찾을 수 없습니다"));

        if (!transaction.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("권한이 없습니다");
        }

        transactionRepository.delete(transaction);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpenseForCurrentMonth(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        BigDecimal result = transactionRepository.getTotalExpenseForPeriod(user, startOfMonth, endOfMonth);
        return result != null ? result : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalIncomeForCurrentMonth(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        BigDecimal result = transactionRepository.getTotalIncomeForPeriod(user, startOfMonth, endOfMonth);
        return result != null ? result : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlySummary(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        BigDecimal totalIncome = getTotalIncomeForCurrentMonth(userEmail);
        BigDecimal totalExpense = getTotalExpenseForCurrentMonth(userEmail);
        BigDecimal netAmount = totalIncome.subtract(totalExpense);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        summary.put("netAmount", netAmount);
        summary.put("period", Map.of("start", startOfMonth, "end", endOfMonth));

        return summary;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getChartData(String userEmail, String type) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        Map<String, Object> chartData = new HashMap<>();
        
        if ("monthly".equals(type)) {
            // 최근 12개월 데이터
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(12);
            
            List<Object[]> rawData = transactionRepository
                .findMonthlyTransactionData(user.getId(), startDate, endDate);
            
            List<Map<String, Object>> monthlyData = rawData.stream()
                .map(row -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("month", row[0]);
                    item.put("type", row[1]);
                    item.put("amount", row[2]);
                    return item;
                })
                .collect(Collectors.toList());
            
            chartData.put("type", "monthly");
            chartData.put("data", monthlyData);
            chartData.put("period", Map.of("start", startDate, "end", endDate));
            
        } else if ("category".equals(type)) {
            // 이번 달 카테고리별 지출 데이터
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
            
            List<Object[]> rawData = transactionRepository
                .findCategoryExpenseData(user.getId(), startOfMonth, endOfMonth);
            
            List<Map<String, Object>> categoryData = rawData.stream()
                .map(row -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("category", row[0]);
                    item.put("color", row[1]);
                    item.put("amount", row[2]);
                    return item;
                })
                .collect(Collectors.toList());
            
            chartData.put("type", "category");
            chartData.put("data", categoryData);
            chartData.put("period", Map.of("start", startOfMonth, "end", endOfMonth));
            
        } else if ("daily".equals(type)) {
            // 이번 달 일별 데이터
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
            
            List<Object[]> rawData = transactionRepository
                .findDailyTransactionData(user.getId(), startOfMonth, endOfMonth);
            
            List<Map<String, Object>> dailyData = rawData.stream()
                .map(row -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", row[0]);
                    item.put("income", row[1]);
                    item.put("expense", row[2]);
                    return item;
                })
                .collect(Collectors.toList());
            
            chartData.put("type", "daily");
            chartData.put("data", dailyData);
            chartData.put("period", Map.of("start", startOfMonth, "end", endOfMonth));
        }
        
        return chartData;
    }
}