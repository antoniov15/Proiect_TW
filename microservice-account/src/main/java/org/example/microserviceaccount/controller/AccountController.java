package org.example.microserviceaccount.controller;

import jakarta.validation.Valid;
import org.example.microserviceaccount.dto.AccountCreateDTO;
import org.example.microserviceaccount.dto.AccountResponseDTO;
import org.example.microserviceaccount.dto.LoginRequestDTO;
import org.example.microserviceaccount.dto.ResetPasswordDTO;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<AccountResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginDTO) {
        AccountResponseDTO responseDTO = accountService.login(loginDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping(value = "/reset-password", produces = "application/json")
    public ResponseEntity<AccountResponseDTO> resetPassword(@Valid @RequestBody ResetPasswordDTO resetDTO) {
        AccountResponseDTO responseDTO = accountService.resetPassword(resetDTO.getEmail(), resetDTO.getNewPassword());
        return ResponseEntity.ok(responseDTO);
    }

    // Create (POST)
    @PostMapping(produces = "application/json")
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody AccountCreateDTO createDTO) {
        AccountResponseDTO responseDTO = accountService.createAccount(createDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    // Read (GET by ID)
    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable Long id) {
        AccountResponseDTO responseDTO = accountService.getAccountById(id);
        return ResponseEntity.ok(responseDTO);
    }

    // Read (GET all)
    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts() {
        org.slf4j.LoggerFactory.getLogger(getClass()).info("GetAll request received");
        List<AccountResponseDTO> responseDTOs = accountService.getAllAccounts();
        return ResponseEntity.ok(responseDTOs);
    }

    // Update (PUT)
    @PutMapping(value = "/{id}", produces = "application/json")
//    @PreAuthorize("@accountSecurity.isOwnerOrAdmin(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<AccountResponseDTO> updateAccount(@PathVariable Long id, @Valid @RequestBody AccountCreateDTO updateDTO) {
        AccountResponseDTO updatedAccount = accountService.updateAccount(id, updateDTO);
        return ResponseEntity.ok(updatedAccount);
    }

    // Delete (DELETE)
    @DeleteMapping("/{id}")
//    @PreAuthorize("@accountSecurity.isOwnerOrAdmin(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    // GET by email
    @GetMapping("/email/{email}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponseDTO> getAccountByEmail(@PathVariable String email) {
        AccountResponseDTO account = accountService.getAccountByEmail(email);
        return ResponseEntity.ok(account);
    }

    // GET ordered by createdAt
    @GetMapping("/sorted")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponseDTO>> getAccountsSortedByCreationDate(
            @RequestParam(defaultValue = "asc") String direction) {
        List<AccountResponseDTO> accounts = accountService.getAccountsSortedByCreationDate(direction);
        return ResponseEntity.ok(accounts);
    }

    // GET by userName
    @GetMapping("/search")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponseDTO>> searchAccountsByUsername(
            @RequestParam String username) {
        List<AccountResponseDTO> accounts = accountService.findAccountsByUsernameContaining(username);
        return ResponseEntity.ok(accounts);
    }
}