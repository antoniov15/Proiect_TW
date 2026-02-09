package com.example.microservice_ai.dto.external;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionResponseDTO {
    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private String type;
    private String categoryName;

    public TransactionResponseDTO() {}

    public TransactionResponseDTO(Long id, BigDecimal amount, LocalDate date, String description, String type, String categoryName) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.type = type;
        this.categoryName = categoryName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
