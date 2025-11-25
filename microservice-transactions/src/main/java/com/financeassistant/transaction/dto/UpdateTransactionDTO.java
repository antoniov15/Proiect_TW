package com.financeassistant.transaction.dto;

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
public class UpdateTransactionDTO {

    private BigDecimal amount;
    private String description;
    private LocalDate date;
    private Long categoryId;
}
