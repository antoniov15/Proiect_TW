package com.financeassistant.transaction.dto;

import com.financeassistant.transaction.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionViewDTO {

    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private TransactionType type;
    private String categoryName;
}
