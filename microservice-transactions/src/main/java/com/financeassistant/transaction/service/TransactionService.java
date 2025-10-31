package com.financeassistant.transaction.service;

import com.financeassistant.transaction.dto.CreateTransactionDTO;
import com.financeassistant.transaction.dto.TransactionViewDTO;
import com.financeassistant.transaction.dto.UpdateTransactionDTO;
import com.financeassistant.transaction.entity.Category;
import com.financeassistant.transaction.entity.Transaction;
import com.financeassistant.transaction.mapper.TransactionMapper;
import com.financeassistant.transaction.repository.CategoryRepository;
import com.financeassistant.transaction.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository, TransactionMapper transactionMapper) {

        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.transactionMapper = transactionMapper;
    }

    @Transactional
    public TransactionViewDTO createTransaction(CreateTransactionDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
            .orElseThrow(() -> new EntityNotFoundException("Invalid category ID"));

        Transaction newTransaction = new Transaction();
        newTransaction.setUserId(dto.getUserId());
        newTransaction.setAmount(dto.getAmount());
        newTransaction.setDate(dto.getDate());
        newTransaction.setDescription(dto.getDescription());
        newTransaction.setCategory(category);
        newTransaction.setType(dto.getType());

        Transaction savedTransaction = transactionRepository.save(newTransaction);

        return transactionMapper.toViewDTO(savedTransaction);
    }

    @Transactional(readOnly = true)
    public TransactionViewDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Transaction not found with ID: " + id));

        return transactionMapper.toViewDTO(transaction);
    }

    @Transactional
    public TransactionViewDTO updateTransaction(Long id, UpdateTransactionDTO dto) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with ID: " + id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + dto.getCategoryId()));

        existingTransaction.setAmount(dto.getAmount());
        existingTransaction.setDate(dto.getDate());
        existingTransaction.setDescription(dto.getDescription());
        existingTransaction.setCategory(category);

        Transaction updatedTransaction = transactionRepository.save(existingTransaction);

        return transactionMapper.toViewDTO(updatedTransaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        if(!transactionRepository.existsById(id)) {
            throw new EntityNotFoundException("Transaction not found with ID: " + id);
        }
        transactionRepository.deleteById(id);
    }
}
