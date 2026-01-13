package com.financeassistant.transaction.controller;

import com.financeassistant.transaction.dto.CreateTransactionDTO;
import com.financeassistant.transaction.dto.SmartTransactionDTO;
import com.financeassistant.transaction.dto.TransactionViewDTO;
import com.financeassistant.transaction.dto.UpdateTransactionDTO;
import com.financeassistant.transaction.entity.TransactionType;
import com.financeassistant.transaction.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/sync-check")
    public ResponseEntity<String> checkSync() {
        return ResponseEntity.ok(transactionService.checkUserSync());
    }

    @PostMapping("/smart")
    public ResponseEntity<TransactionViewDTO> createSmartTransaction(@RequestBody SmartTransactionDTO dto) {
        return new ResponseEntity<>(transactionService.createSmartTransaction(dto), HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<TransactionViewDTO> createTransaction(@Valid @RequestBody CreateTransactionDTO dto) {

        log.info("REST Request to create transaction for userId: {}", dto.getUserId());
        try {
            TransactionViewDTO newTransaction = transactionService.createTransaction(dto);
            return new ResponseEntity<>(newTransaction, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            log.warn("Failed to create transaction: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionViewDTO> getTransactionById(@PathVariable Long id) {

        log.info("REST Request to get transaction with id: {}", id);
        try {
            TransactionViewDTO transaction = transactionService.getTransactionById(id);
            return new ResponseEntity<>(transaction, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            log.warn("Failed to get transaction: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionViewDTO> updateTransaction(@PathVariable Long id, @Valid @RequestBody UpdateTransactionDTO dto) {

        log.info("REST Request to update transaction with id: {}", id);
        try {
            TransactionViewDTO updatedTransaction = transactionService.updateTransaction(id, dto);
            return new ResponseEntity<>(updatedTransaction, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            log.warn("Failed to update transaction: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {

        log.info("REST Request to delete transaction with id: {}", id);
        try {
            transactionService.deleteTransaction(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            log.warn("Failed to delete transaction: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionViewDTO>> getTransactionsByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false, defaultValue = "date") String sortBy,
            @RequestParam(required = false) String order
            ){

        log.info("REST Request to get transactions for userId: {}, type: {}, sortBy: {}", userId, type, sortBy);
        List<TransactionViewDTO> transactions = transactionService.getTransactionsByUserId(
                userId,
                type,
                sortBy,
                order
        );

        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<TransactionViewDTO>> filterTransactionByType(@RequestParam TransactionType type) {
        List<TransactionViewDTO> transactions = transactionService.getTransactionsByType(type);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/sort")
    public ResponseEntity<List<TransactionViewDTO>> sortTransactions(
            @RequestParam String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order
    ) {
        List<TransactionViewDTO> transactions = transactionService.getSortedTransactions(sortBy, order);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }
}
