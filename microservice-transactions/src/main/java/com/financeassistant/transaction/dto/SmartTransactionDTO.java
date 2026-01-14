package com.financeassistant.transaction.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmartTransactionDTO {
    private String description;
    private Double amount;
}
