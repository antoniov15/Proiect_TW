package org.example.microserviceaccount.service;

import org.example.microserviceaccount.client.TransactionClient;
import org.example.microserviceaccount.domain.AccountDomainService;
import org.example.microserviceaccount.dto.AccountCreateDTO;
import org.example.microserviceaccount.dto.AccountResponseDTO;
import org.example.microserviceaccount.dto.AccountSummaryDTO;
import org.example.microserviceaccount.dto.external.TransactionDTO;
import org.example.microserviceaccount.entity.Account;
import org.example.microserviceaccount.mapper.AccountMapper;
import org.example.microserviceaccount.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountDomainService accountDomainService;

    @Mock
    private TransactionClient transactionClient;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_Successful() {
        // Arrange
        AccountCreateDTO accountCreateDTO = new AccountCreateDTO();
        accountCreateDTO.setEmail("test@example.com");
        accountCreateDTO.setUserName("testuser");
        accountCreateDTO.setPassword("password123");

        Account accountEntity = new Account();
        accountEntity.setId(1L);
        accountEntity.setEmail("test@example.com");
        accountEntity.setUserName("testuser");
        accountEntity.setPassword("password123");
        accountEntity.setCreatedAt(LocalDate.now());

        AccountResponseDTO responseDTO = new AccountResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setEmail("test@example.com");
        responseDTO.setUserName("testuser");

        // Mock behavior
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(accountRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        when(accountMapper.accountCreateDTOToAccount(any(AccountCreateDTO.class))).thenReturn(accountEntity);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(accountRepository.save(any(Account.class))).thenReturn(accountEntity);
        when(accountMapper.accountToAccountResponseDTO(any(Account.class))).thenReturn(responseDTO);

        // Act
        AccountResponseDTO result = accountService.createAccount(accountCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void createAccount_ThrowsException_WhenEmailExists() {
        // Arrange
        AccountCreateDTO accountCreateDTO = new AccountCreateDTO();
        accountCreateDTO.setEmail("existing@example.com");
        accountCreateDTO.setUserName("user");
        accountCreateDTO.setPassword("pass");

        when(accountRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(new Account()));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.createAccount(accountCreateDTO);
        });

        assertTrue(exception.getMessage().contains("already in use"));

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void getAccountById_Successful() {
        // Arrange
        Long accountId = 1L;
        Account account = new Account();
        account.setId(accountId);

        AccountResponseDTO expectedResponse = new AccountResponseDTO();
        expectedResponse.setId(accountId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountMapper.accountToAccountResponseDTO(account)).thenReturn(expectedResponse);

        // Act
        AccountResponseDTO result = accountService.getAccountById(accountId);

        // Assert
        assertNotNull(result); // E bine să verifici și că nu e null
        assertEquals(accountId, result.getId());
    }

    @Test
    void getAccountSummary_Successful() {
        // Arrange
        Long accountId = 100L;
        Account account = new Account();
        account.setId(accountId);
        account.setUserName("testuser");
        account.setEmail("test@mail.com");

        // Setup tranzactii simulate de la microserviciul extern
        TransactionDTO incomeTx = new TransactionDTO();
        incomeTx.setAmount(BigDecimal.valueOf(1500));
        incomeTx.setType("INCOME");

        TransactionDTO expenseTx = new TransactionDTO();
        expenseTx.setAmount(BigDecimal.valueOf(500));
        expenseTx.setType("EXPENSE");

        List<TransactionDTO> transactions = Arrays.asList(incomeTx, expenseTx);

        // Mock comportament
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionClient.getTransactionsByUserId(accountId)).thenReturn(transactions);

        // Act
        AccountSummaryDTO summary = accountService.getAccountSummary(accountId);

        // Assert
        assertNotNull(summary);
        assertEquals(accountId, summary.getAccountId());
        assertEquals("testuser", summary.getUserName());
        assertEquals(2, summary.getRecentTransactions().size());

        // Verificam calculul balantei: 1500 (Income) - 500 (Expense) = 1000
        assertEquals(1000.0, summary.getTotalBalanceCalculated());

        verify(transactionClient, times(1)).getTransactionsByUserId(accountId);
    }

    @Test
    void checkAndPromoteToVip_Promotes_WhenIncomeExceedsThreshold() {
        // Arrange
        Long accountId = 200L;
        Account account = new Account();
        account.setId(accountId);
        account.setUserName("regularUser");

        // Income total = 1200 > 1000
        TransactionDTO income1 = new TransactionDTO();
        income1.setAmount(BigDecimal.valueOf(600));
        income1.setType("INCOME");

        TransactionDTO income2 = new TransactionDTO();
        income2.setAmount(BigDecimal.valueOf(600));
        income2.setType("INCOME");

        List<TransactionDTO> transactions = Arrays.asList(income1, income2);

        Account updatedAccount = new Account();
        updatedAccount.setId(accountId);
        updatedAccount.setUserName("regularUser (VIP)");

        AccountResponseDTO responseDTO = new AccountResponseDTO();
        responseDTO.setUserName("regularUser (VIP)");

        // Mock
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionClient.getTransactionsByUserId(accountId)).thenReturn(transactions);
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);
        when(accountMapper.accountToAccountResponseDTO(any(Account.class))).thenReturn(responseDTO);

        // Act
        AccountResponseDTO result = accountService.checkAndPromoteToVip(accountId);

        // Assert
        // Verificam ca s-a apelat save cu numele modificat
        verify(accountRepository).save(argThat(acc -> acc.getUserName().endsWith("(VIP)")));
        assertEquals("regularUser (VIP)", result.getUserName());
    }

    @Test
    void checkAndPromoteToVip_DoesNotPromote_WhenIncomeIsLow() {
        // Arrange
        Long accountId = 300L;
        Account account = new Account();
        account.setId(accountId);
        account.setUserName("poorUser");

        // Income total = 800 <= 1000
        TransactionDTO income1 = new TransactionDTO();
        income1.setAmount(BigDecimal.valueOf(800));
        income1.setType("INCOME");

        List<TransactionDTO> transactions = Collections.singletonList(income1);
        AccountResponseDTO responseDTO = new AccountResponseDTO();
        responseDTO.setUserName("poorUser");

        // Mock
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionClient.getTransactionsByUserId(accountId)).thenReturn(transactions);
        when(accountMapper.accountToAccountResponseDTO(account)).thenReturn(responseDTO);

        // Act
        AccountResponseDTO result = accountService.checkAndPromoteToVip(accountId);

        // Assert
        // Verificam ca NU s-a apelat save
        verify(accountRepository, never()).save(any(Account.class));
        assertEquals("poorUser", result.getUserName());
    }
}