package org.example.microserviceaccount.service;

import org.example.microserviceaccount.dto.AccountCreateDTO;
import org.example.microserviceaccount.dto.AccountResponseDTO;
import org.example.microserviceaccount.entity.Account;
import org.example.microserviceaccount.mapper.AccountMapper;
import org.example.microserviceaccount.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

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
}
