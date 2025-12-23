package com.financeassistant.transaction.domain;

import com.financeassistant.transaction.entity.Category;
import com.financeassistant.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionDomainService {

    public void validateTransactionCreation(BigDecimal amount, TransactionType transactionType, Category category) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (transactionType == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (!category.getType().name().equals(transactionType.name())) {
            throw new IllegalArgumentException("Transaction type does not match category type");
        }
    }

    public void validateMonthlyExpense(int year) {
        int currentYear = LocalDate.now().getYear();
        if (year > currentYear || year < 2000) {
            throw new IllegalArgumentException("Year must be between 2000 and " + currentYear);
        }
    }

    public void validateBudgetCheckRequest(Long userId, BigDecimal budgetLimit) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (budgetLimit == null || budgetLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget limit must be positive");
        }
    }

    public void validateArchiveRequest(LocalDate cutoffDate) {
        if (cutoffDate == null) {
            throw new IllegalArgumentException("Cutoff date cannot be null");
        }
        if (cutoffDate.isAfter(LocalDate.now().minusMonths(1))) {
            throw new IllegalArgumentException("Cutoff date must be at least one month in the past");
        }
    }
}
