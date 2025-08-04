package com.unble.budget.controller;

import com.unble.budget.dto.TransactionRequest;
import com.unble.budget.entity.User;
import com.unble.budget.repository.UserRepository;
import com.unble.budget.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/migration")
public class DataMigrationController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/guest-data")
    public ResponseEntity<Map<String, Object>> migrateGuestData(
            @RequestBody List<TransactionRequest> guestTransactions,
            Authentication authentication) {
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        int successCount = 0;
        int failCount = 0;

        for (TransactionRequest request : guestTransactions) {
            try {
                transactionService.createTransaction(user, request);
                successCount++;
            } catch (Exception e) {
                failCount++;
                System.err.println("거래 마이그레이션 실패: " + e.getMessage());
            }
        }

        Map<String, Object> result = Map.of(
            "success", true,
            "message", "게스트 데이터 마이그레이션이 완료되었습니다.",
            "successCount", successCount,
            "failCount", failCount,
            "totalCount", guestTransactions.size()
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/validate-data")
    public ResponseEntity<Map<String, Object>> validateMigrationData(
            @RequestBody List<TransactionRequest> guestTransactions) {
        
        int validCount = 0;
        int invalidCount = 0;

        for (TransactionRequest request : guestTransactions) {
            if (isValidTransaction(request)) {
                validCount++;
            } else {
                invalidCount++;
            }
        }

        Map<String, Object> result = Map.of(
            "validCount", validCount,
            "invalidCount", invalidCount,
            "totalCount", guestTransactions.size(),
            "canMigrate", invalidCount == 0
        );

        return ResponseEntity.ok(result);
    }

    private boolean isValidTransaction(TransactionRequest request) {
        return request.getAmount() != null && 
               request.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0 &&
               request.getDescription() != null && 
               !request.getDescription().trim().isEmpty() &&
               request.getCategoryId() != null &&
               request.getTransactionDate() != null;
    }
}