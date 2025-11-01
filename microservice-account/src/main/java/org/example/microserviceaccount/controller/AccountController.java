package org.example.microserviceaccount.controller;

import jakarta.validation.Valid;
import org.example.microserviceaccount.dto.AccountCreateDTO;
import org.example.microserviceaccount.dto.AccountResponseDTO;
import org.example.microserviceaccount.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Create (POST)
    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody AccountCreateDTO createDTO) {
        AccountResponseDTO responseDTO = accountService.createAccount(createDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    // Read (GET by ID)
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable Long id) {
        AccountResponseDTO responseDTO = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    // Read (GET all)
    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts() {
        List<AccountResponseDTO> responseDTOs = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    // Update (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> updateAccount(@PathVariable Long id, @Valid @RequestBody AccountCreateDTO updateDTO) {
        AccountResponseDTO updatedAccount = accountService.updateAccount(id, updateDTO);
        return ResponseEntity.ok(updatedAccount);
    }

    // Delete (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build(); // Returnează 204 No Content
    }
}