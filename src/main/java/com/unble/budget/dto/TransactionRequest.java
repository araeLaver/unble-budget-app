package com.unble.budget.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionRequest {
    @NotNull(message = "금액은 필수입니다")
    @Positive(message = "금액은 0보다 커야 합니다")
    private BigDecimal amount;

    @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다")
    private String description;

    @NotNull(message = "거래 유형은 필수입니다")
    private String transactionType; // INCOME or EXPENSE

    @NotNull(message = "거래 날짜는 필수입니다")
    private LocalDate transactionDate;

    private Long categoryId;

    public TransactionRequest() {}

    // Getters and Setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    
    @Override
    public String toString() {
        return "TransactionRequest{" +
                "amount=" + amount +
                ", description='" + description + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", transactionDate=" + transactionDate +
                ", categoryId=" + categoryId +
                '}';
    }
}