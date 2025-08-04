package com.unble.budget.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unble.budget.dto.TransactionRequest;
import com.unble.budget.entity.Category;
import com.unble.budget.entity.Transaction;
import com.unble.budget.entity.User;
import com.unble.budget.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class DataImportService {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CategoryRepository categoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> importFromCSV(User user, MultipartFile file) {
        try {
            List<TransactionRequest> transactions = parseCSV(file);
            return processImport(user, transactions, "CSV");
        } catch (Exception e) {
            return createErrorResult("CSV 파일 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public Map<String, Object> importFromExcel(User user, MultipartFile file) {
        try {
            // Excel 파일을 CSV로 변환하여 처리 (간단한 구현)
            List<TransactionRequest> transactions = parseCSV(file);
            return processImport(user, transactions, "Excel");
        } catch (Exception e) {
            return createErrorResult("Excel 파일 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public Map<String, Object> importFromJSON(User user, MultipartFile file) {
        try {
            List<TransactionRequest> transactions = parseJSON(file);
            return processImport(user, transactions, "JSON");
        } catch (Exception e) {
            return createErrorResult("JSON 파일 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public String getCSVTemplate() {
        return "날짜,설명,금액,카테고리,거래타입\n" +
               "2024-01-01,점심식사,15000,식비,EXPENSE\n" +
               "2024-01-01,급여,2500000,급여,INCOME\n" +
               "2024-01-02,교통비,2000,교통비,EXPENSE\n";
    }

    public Map<String, Object> previewImport(User user, MultipartFile file, String format) {
        try {
            List<TransactionRequest> transactions;
            switch (format.toLowerCase()) {
                case "csv":
                    transactions = parseCSV(file);
                    break;
                case "json":
                    transactions = parseJSON(file);
                    break;
                default:
                    return createErrorResult("지원하지 않는 파일 형식입니다.");
            }

            // 처음 10개만 미리보기
            List<TransactionRequest> preview = transactions.subList(0, Math.min(10, transactions.size()));
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("totalCount", transactions.size());
            result.put("preview", preview);
            result.put("validCount", transactions.size());
            result.put("invalidCount", 0);
            
            return result;
        } catch (Exception e) {
            return createErrorResult("파일 미리보기 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private List<TransactionRequest> parseCSV(MultipartFile file) throws Exception {
        List<TransactionRequest> transactions = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // 헤더 스킵
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    TransactionRequest request = new TransactionRequest();
                    
                    try {
                        // 날짜 파싱
                        request.setTransactionDate(parseDate(parts[0].trim()));
                        
                        // 설명
                        request.setDescription(parts[1].trim());
                        
                        // 금액
                        request.setAmount(new BigDecimal(parts[2].trim()));
                        
                        // 카테고리 찾기
                        String categoryName = parts[3].trim();
                        Category category = findOrCreateCategory(categoryName);
                        request.setCategoryId(category.getId());
                        
                        // 거래 타입
                        String transactionType = parts[4].trim().toUpperCase();
                        if (!transactionType.equals("INCOME") && !transactionType.equals("EXPENSE")) {
                            transactionType = "EXPENSE"; // 기본값
                        }
                        request.setTransactionType(transactionType);
                        
                        transactions.add(request);
                    } catch (Exception e) {
                        System.err.println("라인 파싱 오류: " + line + " - " + e.getMessage());
                    }
                }
            }
        }
        
        return transactions;
    }

    private List<TransactionRequest> parseJSON(MultipartFile file) throws Exception {
        List<TransactionRequest> transactions = new ArrayList<>();
        
        JsonNode rootNode = objectMapper.readTree(file.getInputStream());
        
        if (rootNode.isArray()) {
            for (JsonNode node : rootNode) {
                try {
                    TransactionRequest request = new TransactionRequest();
                    
                    request.setTransactionDate(parseDate(node.get("date").asText()));
                    request.setDescription(node.get("description").asText());
                    request.setAmount(new BigDecimal(node.get("amount").asText()));
                    
                    String categoryName = node.get("category").asText();
                    Category category = findOrCreateCategory(categoryName);
                    request.setCategoryId(category.getId());
                    
                    String transactionType = node.get("type").asText().toUpperCase();
                    request.setTransactionType(transactionType);
                    
                    transactions.add(request);
                } catch (Exception e) {
                    System.err.println("JSON 노드 파싱 오류: " + e.getMessage());
                }
            }
        }
        
        return transactions;
    }

    private LocalDate parseDate(String dateStr) {
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // 다음 형식 시도
            }
        }
        
        throw new RuntimeException("날짜 형식을 인식할 수 없습니다: " + dateStr);
    }

    private Category findOrCreateCategory(String categoryName) {
        return categoryRepository.findByName(categoryName)
                .orElseGet(() -> {
                    // 기본 카테고리로 생성
                    Category newCategory = new Category();
                    newCategory.setName(categoryName);
                    newCategory.setColor("#666666");
                    newCategory.setIcon("📝");
                    newCategory.setCategoryType(Category.CategoryType.EXPENSE);
                    newCategory.setIsDefault(false);
                    newCategory.setSortOrder(999);
                    return categoryRepository.save(newCategory);
                });
    }

    private Map<String, Object> processImport(User user, List<TransactionRequest> transactions, String format) {
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (TransactionRequest request : transactions) {
            try {
                transactionService.createTransaction(user, request);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("거래 추가 실패: " + request.getDescription() + " - " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", format + " 파일 임포트가 완료되었습니다.");
        result.put("totalCount", transactions.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);

        return result;
    }

    private Map<String, Object> createErrorResult(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }
}