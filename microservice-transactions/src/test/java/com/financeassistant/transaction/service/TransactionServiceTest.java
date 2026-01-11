package com.financeassistant.transaction.service;

import com.financeassistant.transaction.dto.CreateTransactionDTO;
import com.financeassistant.transaction.dto.TransactionViewDTO;
import com.financeassistant.transaction.dto.UpdateTransactionDTO;
import com.financeassistant.transaction.entity.Category;
import com.financeassistant.transaction.entity.Transaction;
import com.financeassistant.transaction.entity.TransactionType;
import com.financeassistant.transaction.exception.ResourceNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void createTransaction_ValidInput_ReturnsDTO() {

        CreateTransactionDTO dto = new CreateTransactionDTO();
        dto.setUserId(1L);
        dto.setAmount(new BigDecimal("100.00"));
        dto.setCategoryId(1L);
        dto.setType(TransactionType.EXPENSE);

        Category category = new Category();
        category.setId(1L);
        category.setType(TransactionType.EXPENSE);

        Transaction transactionToSave = new Transaction();
        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(10L);
        savedTransaction.setCategory(category);
        savedTransaction.setType(TransactionType.EXPENSE);

        TransactionViewDTO expectedViewDTO = new TransactionViewDTO();
        expectedViewDTO.setId(10L);

        when(transactionMapper.toEntity(any(CreateTransactionDTO.class))).thenReturn(transactionToSave);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(transactionMapper.toViewDTO(savedTransaction)).thenReturn(expectedViewDTO);

        TransactionViewDTO actualViewDTO = transactionService.createTransaction(dto);

        assertNotNull(actualViewDTO);
        assertEquals(10L, actualViewDTO.getId());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_InvalidCategory_ThrowsException() {

        CreateTransactionDTO dto = new CreateTransactionDTO();
        dto.setCategoryId(99L);

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.createTransaction(dto);
        });

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void getTransactionById_ExistingId_ReturnsDTO() {

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        TransactionViewDTO expectedDTO = new TransactionViewDTO();
        expectedDTO.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toViewDTO(transaction)).thenReturn(expectedDTO);

        TransactionViewDTO actualDTO = transactionService.getTransactionById(1L);

        assertEquals(1L, actualDTO.getId());
    }

    @Test
    void getTransactionById_NonExistingId_ThrowsException() {

        when(transactionRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.getTransactionById(2L);
        });
    }

    @Test
    void updateTransaction_ValidInput_ReturnsUpdatedDTO() {
        Long transactionId = 1L;
        UpdateTransactionDTO dto = new UpdateTransactionDTO();
        dto.setAmount(new BigDecimal("200.00"));
        dto.setCategoryId(2L);

        Transaction existingTransaction = new Transaction();
        existingTransaction.setId(transactionId);

        Category newcategory = new Category();
        newcategory.setId(2L);

        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId(transactionId);
        updatedTransaction.setAmount(new BigDecimal("200.00"));

        TransactionViewDTO expectedDTO = new TransactionViewDTO();
        expectedDTO.setAmount(new BigDecimal("200.00"));

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existingTransaction));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newcategory));
        when(transactionRepository.save(existingTransaction)).thenReturn(updatedTransaction);
        when(transactionMapper.toViewDTO(updatedTransaction)).thenReturn(expectedDTO);

        TransactionViewDTO actualDTO = transactionService.updateTransaction(transactionId, dto);

        assertEquals(new BigDecimal("200.00"), actualDTO.getAmount());
        verify(transactionRepository).save(existingTransaction);
    }

    @Test
    void updateTransaction_TransactionNotFound_ThrowsException() {
        when(transactionRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            transactionService.updateTransaction(20L, new UpdateTransactionDTO())
        );
    }

    @Test
    void updateTransaction_CategoryNotFound_ThrowsException() {

        Transaction existing = new Transaction();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));

        UpdateTransactionDTO dto = new UpdateTransactionDTO();
        dto.setCategoryId(99L);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                transactionService.updateTransaction(1L, dto)
        );
    }

    @Test
    void deleteTransaction_ExistingId_Deletes() {
        when(transactionRepository.existsById(1L)).thenReturn(true);

        transactionService.deleteTransaction(1L);

        verify(transactionRepository).deleteById(1L);
    }

    @Test
    void deleteTransaction_NonExistingId_ThrowsException() {
        when(transactionRepository.existsById(2L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
            transactionService.deleteTransaction(2L)
        );

        verify(transactionRepository, never()).deleteById(anyLong());
    }
}
