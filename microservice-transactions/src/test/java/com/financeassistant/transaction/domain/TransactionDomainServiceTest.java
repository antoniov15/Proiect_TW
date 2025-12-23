package com.financeassistant.transaction.domain;

import com.financeassistant.transaction.entity.Category;
import com.financeassistant.transaction.entity.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionDomainServiceTest {

    @InjectMocks
    private TransactionDomainService transactionDomainService;

    @Test
    @DisplayName("Should pass when creating valid transaction")
    void testValidateTransactionCreation_Valid() {
        Category category = new Category();
        category.setType(TransactionType.EXPENSE);
        assertDoesNotThrow(() ->
                transactionDomainService.validateTransactionCreation(
                        BigDecimal.valueOf(100),
                        TransactionType.EXPENSE,
                        category
                )
        );
    }

    @Test
    @DisplayName("Should throw exception when Amount is negative")
    void testValidateTransactionCreation_NegativeAmount() {
        Category category = new Category();
        category.setType(TransactionType.EXPENSE);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                transactionDomainService.validateTransactionCreation(
                        BigDecimal.valueOf(-50),
                        TransactionType.EXPENSE,
                        category
                )
        );

        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when Type mismatches Category Type")
    void testValidateTransactionCreation_TypeMismatch() {
        Category category = new Category();
        category.setType(TransactionType.INCOME); // Categorie de venit

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                transactionDomainService.validateTransactionCreation(
                        BigDecimal.valueOf(100),
                        TransactionType.EXPENSE, // Încercăm să facem o cheltuială
                        category
                )
        );

        assertEquals("Transaction type does not match category type", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for year older than 2000")
    void testValidateMonthlyExpenseRequest_OldYear() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                transactionDomainService.validateMonthlyExpense(1999)
        );
        assertTrue(exception.getMessage().contains("Year must be between 2000"));
    }

    @Test
    @DisplayName("Should throw exception for archive date in the future")
    void testValidateArchiveRequest_FutureDate() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                transactionDomainService.validateArchiveRequest(LocalDate.now().plusDays(1))
        );
        assertEquals("Cutoff date must be at least one month in the past", exception.getMessage());
    }
}