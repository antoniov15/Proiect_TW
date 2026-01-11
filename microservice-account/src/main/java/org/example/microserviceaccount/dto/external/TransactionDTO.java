package org.example.microserviceaccount.dto.external;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String type; // INCOME/EXPENSE
    private String category;
    private String description;
    private LocalDateTime date;
}
