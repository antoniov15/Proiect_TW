package com.financeassistant.transaction.controller;

import com.financeassistant.transaction.dto.CreateTransactionDTO;
import com.financeassistant.transaction.dto.TransactionViewDTO;
import com.financeassistant.transaction.dto.UpdateTransactionDTO;
import com.financeassistant.transaction.entity.TransactionType;
import com.financeassistant.transaction.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionViewDTO> createTransaction(@RequestBody CreateTransactionDTO dto) {

        try {
            TransactionViewDTO newTransaction = transactionService.createTransaction(dto);
            return new ResponseEntity<>(newTransaction, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionViewDTO> getTransactionById(@PathVariable Long id) {
        try {
            TransactionViewDTO transaction = transactionService.getTransactionById(id);
            return new ResponseEntity<>(transaction, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionViewDTO> updateTransaction(@PathVariable Long id, @RequestBody UpdateTransactionDTO dto) {
        try {
            TransactionViewDTO updatedTransaction = transactionService.updateTransaction(id, dto);
            return new ResponseEntity<>(updatedTransaction, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        try {
            transactionService.deleteTransaction(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
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

        List<TransactionViewDTO> transactions = transactionService.getTransactionsByUserId(
                userId,
                type,
                sortBy,
                order
        );

        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }
}
