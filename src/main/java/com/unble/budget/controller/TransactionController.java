package com.unble.budget.controller;

import com.unble.budget.dto.TransactionRequest;
import com.unble.budget.dto.TransactionResponse;
import com.unble.budget.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@Valid @RequestBody TransactionRequest request, 
                                              Authentication authentication) {
        try {
            TransactionResponse response = transactionService.createTransaction(
                authentication.getName(), request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("거래 생성 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", "거래 추가 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserTransactions(Authentication authentication,
                                                @RequestParam(required = false) 
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
                                                LocalDate startDate,
                                                @RequestParam(required = false) 
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
                                                LocalDate endDate) {
        try {
            List<TransactionResponse> transactions;
            
            if (startDate != null && endDate != null) {
                transactions = transactionService.getUserTransactionsByDateRange(
                    authentication.getName(), startDate, endDate);
            } else {
                transactions = transactionService.getUserTransactions(authentication.getName());
            }
            
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable Long id,
                                              @Valid @RequestBody TransactionRequest request,
                                              Authentication authentication) {
        try {
            TransactionResponse response = transactionService.updateTransaction(
                authentication.getName(), id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id,
                                              Authentication authentication) {
        try {
            transactionService.deleteTransaction(authentication.getName(), id);
            Map<String, String> success = new HashMap<>();
            success.put("message", "거래가 삭제되었습니다");
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getMonthlySummary(Authentication authentication) {
        try {
            Map<String, Object> summary = transactionService.getMonthlySummary(authentication.getName());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/chart-data")
    public ResponseEntity<?> getChartData(Authentication authentication,
                                         @RequestParam(required = false, defaultValue = "monthly") String type) {
        try {
            Map<String, Object> chartData = transactionService.getChartData(authentication.getName(), type);
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}