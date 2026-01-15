package com.financeassistant.transaction.service;

import com.financeassistant.transaction.client.AIFeignClient;
import com.financeassistant.transaction.client.AccountFeignClient;
import com.financeassistant.transaction.dto.*;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

    @Mock private AccountFeignClient accountFeignClient;
    @Mock private AIFeignClient aiFeignClient;
    @Mock private SecurityContext securityContext;
    @Mock private JwtAuthenticationToken jwtAuthenticationToken;

    @InjectMocks
    private TransactionService transactionService;


    @Test
    void createSmartTransaction_ShouldSucceed() {
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getTokenAttributes()).thenReturn(Map.of("email", "test@gmail.com"));
        SecurityContextHolder.setContext(securityContext);

        SmartTransactionDTO dto = new SmartTransactionDTO();
        dto.setAmount(100.0);
        dto.setDescription("KFC Wings");

        Category foodCategory = new Category();
        foodCategory.setName("Food");
        foodCategory.setType(TransactionType.EXPENSE);

        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(1L);

        TransactionViewDTO viewDTO = new TransactionViewDTO();
        viewDTO.setId(1L);

        when(accountFeignClient.getUserIdByEmail("test@gmail.com")).thenReturn(10L);
        when(aiFeignClient.predictCategory("KFC Wings")).thenReturn("Food");
        when(categoryRepository.findByName("Food")).thenReturn(foodCategory);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(transactionMapper.toViewDTO(savedTransaction)).thenReturn(viewDTO);

        TransactionViewDTO result = transactionService.createSmartTransaction(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(transactionRepository).save(any(Transaction.class));
        verify(aiFeignClient).predictCategory("KFC Wings");
    }

    @Test
    void getGlobalAdminReport_ShouldHandlePartialFailures() {
        Transaction t1 = new Transaction(); t1.setUserId(1L); t1.setAmount(BigDecimal.valueOf(100));
        Transaction t2 = new Transaction(); t2.setUserId(2L); t2.setAmount(BigDecimal.valueOf(50));

        when(transactionRepository.findAll()).thenReturn(Arrays.asList(t1, t2));

        AccountResponseDTO acc1 = new AccountResponseDTO(); acc1.setName("User1"); acc1.setEmail("u1@test.com");
        when(accountFeignClient.getAccountById(1L)).thenReturn(acc1);

        when(accountFeignClient.getAccountById(2L)).thenThrow(new RuntimeException("Service Down"));

        List<String> report = transactionService.getGlobalAdminReport();

        assertEquals(2, report.size());
        assertTrue(report.stream().anyMatch(line -> line.contains("User1") && line.contains("100.00 RON")));
        assertTrue(report.stream().anyMatch(line -> line.contains("Account Info Unavailable")));
    }

    @Test
    void checkUserSync_ShouldReturnSuccessMessage() {
        setupSecurityContext("test@gmail.com");
        when(accountFeignClient.getUserIdByEmail("test@gmail.com")).thenReturn(5L);

        String result = transactionService.checkUserSync();

        assertTrue(result.contains("Sync Successful"));
        assertTrue(result.contains("ID: 5"));
    }

    @Test
    void createSmartTransaction_WhenCategoryNotFound_ShouldThrowException() {
        setupSecurityContext("test@gmail.com");
        when(accountFeignClient.getUserIdByEmail("test@gmail.com")).thenReturn(1L);

        when(aiFeignClient.predictCategory("Rocket fuel")).thenReturn("Spaceship");
        when(categoryRepository.findByName("Spaceship")).thenReturn(null);

        SmartTransactionDTO dto = new SmartTransactionDTO("Rocket fuel", 500.0);

        assertThrows(ResourceNotFoundException.class, () ->
                transactionService.createSmartTransaction(dto)
        );
    }

    @Test
    void getTransactionsByUserId_ShouldFilterAndSort() {
        Transaction t1 = new Transaction(); t1.setAmount(BigDecimal.valueOf(10)); t1.setDate(LocalDate.now()); t1.setType(TransactionType.EXPENSE);
        Transaction t2 = new Transaction(); t2.setAmount(BigDecimal.valueOf(20)); t2.setDate(LocalDate.now().minusDays(1)); t2.setType(TransactionType.EXPENSE);

        when(transactionRepository.findAllByUserId(1L)).thenReturn(Arrays.asList(t1, t2));
        when(transactionMapper.toViewDTO(any())).thenReturn(new TransactionViewDTO());

        transactionService.getTransactionsByUserId(1L, TransactionType.EXPENSE, "amount", "desc");

        transactionService.getTransactionsByUserId(1L, null, "date", "asc");

        verify(transactionRepository, times(2)).findAllByUserId(1L);
    }

    @Test
    void getSortedTransactions_ShouldSortByDefault() {
        Transaction t1 = new Transaction(); t1.setDate(LocalDate.now());
        Transaction t2 = new Transaction(); t2.setDate(LocalDate.now().minusDays(1));

        when(transactionRepository.findAll()).thenReturn(Arrays.asList(t1, t2));
        when(transactionMapper.toViewDTO(any())).thenReturn(new TransactionViewDTO());

        transactionService.getSortedTransactions("unknown_field", "asc");

        verify(transactionRepository).findAll();
    }

    private void setupSecurityContext(String email) {
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getTokenAttributes()).thenReturn(Map.of("email", email));
        SecurityContextHolder.setContext(securityContext);
    }

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
