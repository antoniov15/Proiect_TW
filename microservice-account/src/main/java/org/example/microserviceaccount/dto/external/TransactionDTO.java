package org.example.microserviceaccount.dto.external;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionDTO {
    private Long id;
    private BigDecimal amount;
    private String type;
    private String categoryName;
    private String description;
    private LocalDate date;
}