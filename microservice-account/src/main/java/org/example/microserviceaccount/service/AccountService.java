package org.example.microserviceaccount.service;

import jakarta.validation.Valid;
import org.example.microserviceaccount.domain.AccountDomainService;
import org.example.microserviceaccount.dto.AccountCreateDTO;
import org.example.microserviceaccount.dto.AccountResponseDTO;
import org.example.microserviceaccount.dto.LoginRequestDTO;
import org.example.microserviceaccount.entity.Account;
import org.example.microserviceaccount.exception.ResourceNotFoundException;
import org.example.microserviceaccount.mapper.AccountMapper;
import org.example.microserviceaccount.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.microserviceaccount.client.TransactionClient;
import org.example.microserviceaccount.dto.AccountSummaryDTO;
import org.example.microserviceaccount.dto.external.TransactionDTO;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final AccountDomainService accountDomainService;
    private final TransactionClient transactionClient;

    @Autowired
    public AccountService(
            AccountRepository accountRepository,
            AccountMapper accountMapper,
            PasswordEncoder passwordEncoder,
            AccountDomainService accountDomainService,
            TransactionClient transactionClient) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
        this.accountDomainService = accountDomainService;
        this.transactionClient = transactionClient;
    }

    /// CRUD
    // Create POST
    @Transactional
    public AccountResponseDTO createAccount(AccountCreateDTO createDTO) {
        // verif mail si username
        accountRepository.findByEmail(createDTO.getEmail()).ifPresent(account -> {
            throw new IllegalArgumentException("Email " + createDTO.getEmail() + " already in use");
        });

        accountRepository.findByUserName(createDTO.getUserName()).ifPresent(account -> {
            throw new IllegalArgumentException("Username " + createDTO.getUserName() + " already in use");
        });

        Account newAccount = accountMapper.accountCreateDTOToAccount(createDTO);

        newAccount.setPassword(passwordEncoder.encode(newAccount.getPassword()));

        Account savedAccount = accountRepository.save(newAccount);
        return accountMapper.accountToAccountResponseDTO(savedAccount);
    }

    // Read (Get by ID)
    @Transactional(readOnly = true)
    public AccountResponseDTO getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + id + " not found"));
        return accountMapper.accountToAccountResponseDTO(account);
    }

    // Read (Get all)
    @Transactional(readOnly = true)
    public List<AccountResponseDTO> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        return accountMapper.accountsToAccountResponseDTOs(accounts);
    }

    // Update PUT
    @Transactional
    public AccountResponseDTO updateAccount(Long id, AccountCreateDTO updateDTO) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + id + " not found"));

        accountRepository.findByEmail(updateDTO.getEmail()).ifPresent(account -> {
            if (!account.getId().equals(id)) {
                throw new IllegalArgumentException("Email " + updateDTO.getEmail() + " already in use");
            }
        });

        accountRepository.findByUserName(updateDTO.getUserName()).ifPresent(account -> {
            if (!account.getId().equals(id)) {
                throw new IllegalArgumentException("Username " + updateDTO.getUserName() + " already in use");
            }
        });

        existingAccount.setUserName(updateDTO.getUserName());
        existingAccount.setEmail(updateDTO.getEmail());

        Account updatedAccount = accountRepository.save(existingAccount);

        return accountMapper.accountToAccountResponseDTO(updatedAccount);
    }

    // DELETE
    @Transactional
    public void deleteAccount(Long id) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + id + " not found"));
        accountRepository.delete(existingAccount);
    }

    // GET by email
    @Transactional(readOnly = true)
    public AccountResponseDTO getAccountByEmail(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Contul cu email-ul " + email + " nu a fost găsit."));

        return accountMapper.accountToAccountResponseDTO(account);
    }

    // GET ordered by createdAt
    @Transactional(readOnly = true)
    public List<AccountResponseDTO> getAccountsSortedByCreationDate(String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(sortDirection, "createdAt");

        List<Account> accounts = accountRepository.findAll(sort);
        return accountMapper.accountsToAccountResponseDTOs(accounts);
    }

    // GET by userName
    @Transactional(readOnly = true)
    public List<AccountResponseDTO> findAccountsByUsernameContaining(String usernameFragment) {
        List<Account> accounts = accountRepository.findByUserNameContainingIgnoreCase(usernameFragment);
        return accountMapper.accountsToAccountResponseDTOs(accounts);
    }

    // authentication method
    @Transactional(readOnly = true)
    public AccountResponseDTO login(LoginRequestDTO loginDTO) {
        Account account = accountRepository.findByEmail(loginDTO.getLoginIdentifier())
                .or(() -> accountRepository.findByUserName(loginDTO.getLoginIdentifier()))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials"));

        if (passwordEncoder.matches(loginDTO.getPassword(), account.getPassword())) {
            return accountMapper.accountToAccountResponseDTO(account);
        } else {
            throw new ResourceNotFoundException("Invalid credentials");
        }
    }

    //reset password
    @Transactional
    public AccountResponseDTO resetPassword(String email, String newPassword) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account with email " + email + " not found"));

        account.setPassword(passwordEncoder.encode(newPassword));

        Account updatedAccount = accountRepository.save(account);
        return accountMapper.accountToAccountResponseDTO(updatedAccount);
    }

    // Stored Procedures with domain validation
    @Transactional(readOnly = true)
    public Integer countNewUsers(LocalDate startDate, LocalDate endDate) {
        // Domain validation for date range
        accountDomainService.validateDateRange(startDate, endDate);

        return accountRepository.countNewUsers(startDate, endDate);
    }

    @Transactional
    public String anonymizeUserData(Long userId) {
        // Domain validation for anonymization
        accountDomainService.validateAnonymizationRequest(userId);

        return accountRepository.anonymizeUserData(userId);
    }

    @Transactional(readOnly = true)
    public String checkAccountAvailability(String email, String username) {
        // Domain validation
        accountDomainService.validateEmail(email);
        accountDomainService.validateUsername(username);

        return accountRepository.checkAccountAvailability(email, username);
    }

    // viza 3, GET
    @Transactional(readOnly = true)
    public AccountSummaryDTO getAccountSummary(Long accountId) {
        // account
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + accountId + " not found"));

        // apelare Transactions
        // ID-ul contului din acest serviciu corespunde userId-ului din Transactions
        List<TransactionDTO> transactions = transactionClient.getTransactionsByUserId(accountId);

        // procesare
        double totalBalance = transactions.stream()
                .mapToDouble(t -> {
                    if ("INCOME".equalsIgnoreCase(t.getType())) {
                        return t.getAmount().doubleValue();
                    } else {
                        return -t.getAmount().doubleValue();
                    }
                })
                .sum();

        AccountSummaryDTO summary = new AccountSummaryDTO();
        summary.setAccountId(account.getId());
        summary.setUserName(account.getUserName());
        summary.setEmail(account.getEmail());
        summary.setRecentTransactions(transactions);
        summary.setTotalBalanceCalculated(totalBalance);

        return summary;
    }
}