package com.unble.budget.dto;

import com.unble.budget.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private String description;
    private String transactionType;
    private String transactionTypeDisplay;
    private LocalDate transactionDate;
    private CategoryResponse category;
    private LocalDateTime createdAt;

    public TransactionResponse() {}

    public TransactionResponse(Transaction transaction) {
        this.id = transaction.getId();
        this.amount = transaction.getAmount();
        this.description = transaction.getDescription();
        this.transactionType = transaction.getTransactionType().toString();
        this.transactionTypeDisplay = transaction.getTransactionType().getDisplayName();
        this.transactionDate = transaction.getTransactionDate();
        this.createdAt = transaction.getCreatedAt();
        
        if (transaction.getCategory() != null) {
            this.category = new CategoryResponse(transaction.getCategory());
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getTransactionTypeDisplay() { return transactionTypeDisplay; }
    public void setTransactionTypeDisplay(String transactionTypeDisplay) { this.transactionTypeDisplay = transactionTypeDisplay; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public CategoryResponse getCategory() { return category; }
    public void setCategory(CategoryResponse category) { this.category = category; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}