package com.financeassistant.transaction.service;

import com.financeassistant.transaction.dto.CreateTransactionDTO;
import com.financeassistant.transaction.dto.TransactionViewDTO;
import com.financeassistant.transaction.dto.UpdateTransactionDTO;
import com.financeassistant.transaction.entity.Category;
import com.financeassistant.transaction.entity.Transaction;
import com.financeassistant.transaction.entity.TransactionType;
import com.financeassistant.transaction.mapper.TransactionMapper;
import com.financeassistant.transaction.repository.CategoryRepository;
import com.financeassistant.transaction.repository.TransactionRepository;
import com.financeassistant.transaction.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            .orElseThrow(() -> new ResourceNotFoundException("Invalid category ID"));

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
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        return transactionMapper.toViewDTO(transaction);
    }

    @Transactional
    public TransactionViewDTO updateTransaction(Long id, UpdateTransactionDTO dto) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.getCategoryId()));

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
            throw new ResourceNotFoundException("Transaction not found with ID: " + id);
        }
        transactionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<TransactionViewDTO> getTransactionsByUserId(Long userId, TransactionType type, String sortBy, String order) {

        List<Transaction> transactions = transactionRepository.findAllByUserId(userId);

        Stream<Transaction> transactionStream = transactions.stream();

        if (type != null) {
            transactionStream = transactionStream.filter(t -> t.getType() == type);
        }

        Comparator<Transaction> comparator = createComparator(sortBy, order);

        transactionStream = transactionStream.sorted(comparator);

        return transactionStream
                .map(transactionMapper::toViewDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionViewDTO> getTransactionsByType(TransactionType type) {

        List<Transaction> allTransactions = transactionRepository.findAll();
        return allTransactions.stream()
                .filter(t -> t.getType() == type)
                .map(transactionMapper::toViewDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionViewDTO> getSortedTransactions(String sortBy, String order) {

        List<Transaction> allTransactions = transactionRepository.findAll();

        Comparator<Transaction> comparator = createComparator(sortBy, order);

        return allTransactions.stream()
                .sorted(comparator)
                .map(transactionMapper::toViewDTO)
                .collect(Collectors.toList());
    }

    private Comparator<Transaction> createComparator(String sortBy, String order) {
        Comparator<Transaction> comparator = switch (sortBy.toLowerCase()) {
            case "amount" -> Comparator.comparing(Transaction::getAmount);
            case "date" -> Comparator.comparing(Transaction::getDate);
            default -> Comparator.comparing(Transaction::getDate);
        };

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    @Transactional(readOnly = true)
    public BigDecimal getMonthlyExpense(Long userId, int month, int year) {

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        int currentYear = LocalDate.now().getYear();
        if (year < 2000 || year > currentYear) {
            throw new IllegalArgumentException("Year must be between 2000 and " + currentYear);
        }
        return transactionRepository.calculateMonthlyExpense(userId, month, year);
    }

    @Transactional(readOnly = true)
    public String checkBudgetStatus(Long userId, BigDecimal budgetLimit) {

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (budgetLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget limit must be positive");
        }
        return transactionRepository.checkBudgetStatus(userId, budgetLimit);
    }

    @Transactional
    public Integer archiveOldTransactions(LocalDate cutoffDate) {

        if (cutoffDate == null) {
            throw new IllegalArgumentException("Cutoff date cannot be null");
        }

        if (cutoffDate.isAfter(LocalDate.now().minusMonths(1))) {
            throw new IllegalArgumentException("Cutoff date must be at least one month in the past");
        }
        return transactionRepository.archiveOldTransactions(cutoffDate);
    }
}
