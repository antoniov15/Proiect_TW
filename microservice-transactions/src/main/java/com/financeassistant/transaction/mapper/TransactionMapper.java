package com.financeassistant.transaction.mapper;

import com.financeassistant.transaction.dto.TransactionViewDTO;
import com.financeassistant.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionMapper {

    public TransactionViewDTO toViewDTO(Transaction transaction) {
        String categoryName = (transaction.getCategory() != null) ?
        transaction.getCategory().getName() :
        null;

        return new TransactionViewDTO(
            transaction.getId(),
            transaction.getAmount(),
            transaction.getDate(),
            transaction.getDescription(),
            transaction.getType(),
            categoryName
        );
    }

    public List<TransactionViewDTO> toViewDTOs(List<Transaction> transactions) {
        List<TransactionViewDTO> DTOs = new ArrayList<>();
        for (Transaction transaction : transactions) {
            DTOs.add(toViewDTO(transaction));
        }
        return DTOs;
    }
}
