package org.example.microserviceaccount.service;

import jakarta.validation.Valid;
import org.example.microserviceaccount.dto.AccountCreateDTO;
import org.example.microserviceaccount.dto.AccountResponseDTO;
import org.example.microserviceaccount.dto.LoginRequestDTO;
import org.example.microserviceaccount.dto.ResetPasswordDTO;
import org.example.microserviceaccount.entity.Account;
import org.example.microserviceaccount.exception.ResourceNotFoundException;
import org.example.microserviceaccount.mapper.AccountMapper;
import org.example.microserviceaccount.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /// CRUD
    // Create POST
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
    public AccountResponseDTO getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + id + " not found"));
        return accountMapper.accountToAccountResponseDTO(account);
    }

    // Read (Get all)
    public List<AccountResponseDTO> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        return accountMapper.accountsToAccountResponseDTOs(accounts);
    }

    // Update PUT
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
    public void deleteAccount(Long id) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + id + " not found"));
        accountRepository.delete(existingAccount);
    }

    // GET by email
    public AccountResponseDTO getAccountByEmail(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Contul cu email-ul " + email + " nu a fost găsit."));

        return accountMapper.accountToAccountResponseDTO(account);
    }

    // GET ordered by createdAt
    public List<AccountResponseDTO> getAccountsSortedByCreationDate(String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(sortDirection, "createdAt");

        List<Account> accounts = accountRepository.findAll(sort);
        return accountMapper.accountsToAccountResponseDTOs(accounts);
    }

    // GET by userName
    public List<AccountResponseDTO> findAccountsByUsernameContaining(String usernameFragment) {
        List<Account> accounts = accountRepository.findByUserNameContainingIgnoreCase(usernameFragment);
        return accountMapper.accountsToAccountResponseDTOs(accounts);
    }

    // authentication method
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

    public AccountResponseDTO resetPassword(String email, String newPassword) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account with email " + email + " not found"));

        account.setPassword(passwordEncoder.encode(newPassword));

        Account updatedAccount = accountRepository.save(account);
        return accountMapper.accountToAccountResponseDTO(updatedAccount);
    }
}