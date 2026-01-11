package org.example.microserviceaccount.dto;

import lombok.Data;
import org.example.microserviceaccount.dto.external.TransactionDTO;
import java.util.List;

@Data
public class AccountSummaryDTO {
    // Date din Account Service
    private Long accountId;
    private String userName;
    private String email;

    // Date din Transaction Service
    private List<TransactionDTO> recentTransactions;
    private Double totalBalanceCalculated;
}
