package com.financeassistant.transaction.service;

import com.financeassistant.transaction.mapper.TransactionMapper;
import com.financeassistant.transaction.repository.CategoryRepository;
import com.financeassistant.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;


    @Test
    void getMonthlyExpense_ValidInputs_ReturnsAmount() {
        Long userId = 1L;
        int month = 5;
        int year = 2024;
        BigDecimal expectedAmount = new BigDecimal("1500.00");

        when(transactionRepository.calculateMonthlyExpense(userId, month, year))
            .thenReturn(expectedAmount);

        BigDecimal actualAmount = transactionService.getMonthlyExpense(userId, month, year);

        assertEquals(expectedAmount, actualAmount);
        verify(transactionRepository).calculateMonthlyExpense(userId, month, year);
    }

    @Test
    void getMonthlyExpense_NullUserId_ThrowsException() {

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.getMonthlyExpense(null, 5, 2024);
        });
    }

    @Test
    void getMonthlyExpense_InvalidMonth_ThrowsException() {

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.getMonthlyExpense(1L, 13, 2024);
        });
    }

    @Test
    void checkBudgetStatus_WithinBudget_ReturnsString() {
        Long userId = 1L;
        BigDecimal budgetLimit = new BigDecimal("2000.00");
        String expectedStatus = "Within Budget";

        when(transactionRepository.checkBudgetStatus(userId, budgetLimit))
            .thenReturn(expectedStatus);

        String actualStatus = transactionService.checkBudgetStatus(userId, budgetLimit);

        assertEquals(expectedStatus, actualStatus);
    }

    @Test
    void archiveOldTransactions_ValidDate_ReturnsCount() {
        LocalDate oldDate = LocalDate.now().minusYears(1);
        when(transactionRepository.archiveOldTransactions(oldDate)).thenReturn(15);

        Integer deletedCount = transactionService.archiveOldTransactions(oldDate);

        assertEquals(15, deletedCount);
    }

    @Test
    void archiveOldTransactions_TooRecentDate_ThrowsException() {
        LocalDate recentDate = LocalDate.now().minusDays(10);

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.archiveOldTransactions(recentDate);
        });

        verify(transactionRepository, never()).archiveOldTransactions(any());
    }
}
