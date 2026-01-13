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

    // --- UPDATE ---
    @Test
    void updateAccount_Successful() {
        Long id = 1L;
        AccountCreateDTO updateDTO = new AccountCreateDTO();
        updateDTO.setEmail("updated@test.com");
        updateDTO.setUserName("updatedUser");
        updateDTO.setPassword("irrelevant");

        Account existingAccount = new Account();
        existingAccount.setId(id);
        existingAccount.setEmail("old@test.com");
        existingAccount.setUserName("oldUser");

        Account updatedAccountEntity = new Account();
        updatedAccountEntity.setId(id);
        updatedAccountEntity.setEmail("updated@test.com");
        updatedAccountEntity.setUserName("updatedUser");

        AccountResponseDTO responseDTO = new AccountResponseDTO();
        responseDTO.setId(id);
        responseDTO.setEmail("updated@test.com");
        responseDTO.setUserName("updatedUser");

        when(accountRepository.findById(id)).thenReturn(Optional.of(existingAccount));
        // Asigurăm unicitatea (nu găsim alt cont cu noile date)
        when(accountRepository.findByEmail("updated@test.com")).thenReturn(Optional.empty());
        when(accountRepository.findByUserName("updatedUser")).thenReturn(Optional.empty());

        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccountEntity);
        when(accountMapper.accountToAccountResponseDTO(any(Account.class))).thenReturn(responseDTO);

        AccountResponseDTO result = accountService.updateAccount(id, updateDTO);

        assertNotNull(result);
        assertEquals("updatedUser", result.getUserName());
        verify(accountRepository).save(existingAccount); // Verificăm că s-a salvat entitatea existentă modificată
    }

    @Test
    void updateAccount_ThrowsException_WhenEmailTakenByOther() {
        Long id = 1L;
        AccountCreateDTO updateDTO = new AccountCreateDTO();
        updateDTO.setEmail("taken@test.com");

        Account existingAccount = new Account();
        existingAccount.setId(id);

        Account otherAccount = new Account();
        otherAccount.setId(2L); // Alt ID
        otherAccount.setEmail("taken@test.com");

        when(accountRepository.findById(id)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.findByEmail("taken@test.com")).thenReturn(Optional.of(otherAccount));

        assertThrows(IllegalArgumentException.class, () -> accountService.updateAccount(id, updateDTO));
    }

    // --- DELETE ---
    @Test
    void deleteAccount_Successful() {
        Long id = 1L;
        Account account = new Account();
        account.setId(id);

        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        accountService.deleteAccount(id);

        verify(accountRepository).delete(account);
    }

    @Test
    void deleteAccount_ThrowsException_WhenNotFound() {
        Long id = 999L;
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(org.example.microserviceaccount.exception.ResourceNotFoundException.class,
                () -> accountService.deleteAccount(id));
    }

    // --- LISTS (GetAll, Sorted, Search) ---
    @Test
    void getAllAccounts_ReturnsList() {
        List<Account> accounts = Arrays.asList(new Account(), new Account());
        List<AccountResponseDTO> dtos = Arrays.asList(new AccountResponseDTO(), new AccountResponseDTO());

        when(accountRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(accounts);
        when(accountMapper.accountsToAccountResponseDTOs(accounts)).thenReturn(dtos);

        List<AccountResponseDTO> result = accountService.getAllAccounts();

        assertEquals(2, result.size());
    }

    @Test
    void getAccountsSortedByCreationDate_ReturnsList() {
        List<Account> accounts = Collections.singletonList(new Account());
        List<AccountResponseDTO> dtos = Collections.singletonList(new AccountResponseDTO());

        // Verificăm sortarea descrescătoare
        when(accountRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(accounts);
        when(accountMapper.accountsToAccountResponseDTOs(accounts)).thenReturn(dtos);

        List<AccountResponseDTO> result = accountService.getAccountsSortedByCreationDate("desc");

        assertEquals(1, result.size());
        verify(accountRepository).findAll(argThat((org.springframework.data.domain.Sort sort) ->
                sort.getOrderFor("createdAt").getDirection().isDescending()));
    }

    // --- AUTH (Login & Reset) ---
    @Test
    void login_Successful() {
        org.example.microserviceaccount.dto.LoginRequestDTO loginDTO = new org.example.microserviceaccount.dto.LoginRequestDTO();
        loginDTO.setLoginIdentifier("user@test.com");
        loginDTO.setPassword("rawPass");

        Account account = new Account();
        account.setEmail("user@test.com");
        account.setPassword("encodedPass");

        AccountResponseDTO responseDTO = new AccountResponseDTO();
        responseDTO.setEmail("user@test.com");

        when(accountRepository.findByEmail("user@test.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(true);
        when(accountMapper.accountToAccountResponseDTO(account)).thenReturn(responseDTO);

        AccountResponseDTO result = accountService.login(loginDTO);

        assertNotNull(result);
    }

    @Test
    void login_ThrowsException_InvalidPassword() {
        org.example.microserviceaccount.dto.LoginRequestDTO loginDTO = new org.example.microserviceaccount.dto.LoginRequestDTO();
        loginDTO.setLoginIdentifier("user@test.com");
        loginDTO.setPassword("wrongPass");

        Account account = new Account();
        account.setPassword("encodedPass");

        when(accountRepository.findByEmail("user@test.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        assertThrows(org.example.microserviceaccount.exception.ResourceNotFoundException.class,
                () -> accountService.login(loginDTO));
    }

    @Test
    void resetPassword_Successful() {
        String email = "test@test.com";
        String newPass = "newPass";
        Account account = new Account();
        account.setEmail(email);

        AccountResponseDTO responseDTO = new AccountResponseDTO();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        when(passwordEncoder.encode(newPass)).thenReturn("encodedNewPass");
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.accountToAccountResponseDTO(account)).thenReturn(responseDTO);

        AccountResponseDTO result = accountService.resetPassword(email, newPass);

        assertNotNull(result);
        verify(passwordEncoder).encode(newPass);
    }
}