package org.example.microserviceaccount.service;

import org.example.microserviceaccount.dto.AccountCreateDTO;
import org.example.microserviceaccount.dto.AccountResponseDTO;
import org.example.microserviceaccount.entity.Account;
import org.example.microserviceaccount.exception.ResourceNotFoundException;
import org.example.microserviceaccount.mapper.AccountMapper;
import org.example.microserviceaccount.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Autowired
    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
}

// metode CRUD
// Create
    public AccountResponseDTO createAccount(AccountCreateDTO createDTO) {
        accountRepository.findByEmail(createDTO.getEmail()).ifPresent(account -> {
            throw new IllegalArgumentException("Email " + createDTO.getEmail() + " already in use");}
        });
        Account newAccount = accountMapper.accountCreateDTOToAccount(createDTO);
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

    // Update
    public AccountResponseDTO updateAccount(Long id, AccountCreateDTO updateDTO) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + id + " not found"));

        existingAccount.setUserName(updateDTO.getUserName());
        existingAccount.setEmail(updateDTO.getEmail());

        Account updatedAccount = accountRepository.save(existingAccount);

        return accountMapper.accountToAccountResponseDTO(updatedAccount);A
    }

    // Delete
    public void deleteAccount(Long id) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + id + " not found"));
        accountRepository.delete(existingAccount);
    }