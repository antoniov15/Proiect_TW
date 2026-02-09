package com.example.microservice_ai.dto.external;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateTransactionRequestDTO {
    private Long userId;
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private String type; // INCOME or EXPENSE
    private Long categoryId;

    public CreateTransactionRequestDTO() {}

    public CreateTransactionRequestDTO(Long userId, BigDecimal amount, LocalDate date, String description, String type, Long categoryId) {
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.type = type;
        this.categoryId = categoryId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
