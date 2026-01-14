package com.financeassistant.transaction.service;

import com.financeassistant.transaction.client.AIFeignClient;
import com.financeassistant.transaction.client.AccountFeignClient;
import com.financeassistant.transaction.domain.TransactionDomainService;
import com.financeassistant.transaction.dto.CreateTransactionDTO;
import com.financeassistant.transaction.dto.SmartTransactionDTO;
import com.financeassistant.transaction.dto.TransactionViewDTO;
import com.financeassistant.transaction.dto.UpdateTransactionDTO;
import com.financeassistant.transaction.entity.Category;
import com.financeassistant.transaction.entity.Transaction;
import com.financeassistant.transaction.entity.TransactionType;
import com.financeassistant.transaction.mapper.TransactionMapper;
import com.financeassistant.transaction.repository.CategoryRepository;
import com.financeassistant.transaction.repository.TransactionRepository;
import com.financeassistant.transaction.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionDomainService transactionDomainService;
    private final AccountFeignClient accountClient;
    private final AIFeignClient aiClient;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository,
                              TransactionMapper transactionMapper,
                              AccountFeignClient accountClient,
                              AIFeignClient aiClient) {

        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.transactionMapper = transactionMapper;
        this.transactionDomainService = new TransactionDomainService();
        this.accountClient = accountClient;
        this.aiClient = aiClient;
    }

    public String checkUserSync() {
        String email = getCurrentUserEmail();
        Long userId = accountClient.getUserIdByEmail(email);
        return "Sync Successful! Email: " + email + " is mapped to ID: " + userId;
    }

    @Transactional
    public TransactionViewDTO createSmartTransaction(SmartTransactionDTO dto) {

        String email = getCurrentUserEmail();
        Long userId = accountClient.getUserIdByEmail(email);

        String predictedCategoryName = aiClient.predictCategory(dto.getDescription());

        String cleanName = predictedCategoryName.strip();
        Category category = categoryRepository.findByName(cleanName);

        if (category == null) {
            throw new ResourceNotFoundException("Category predicted by AI not found in DB: " + cleanName);
        }

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(BigDecimal.valueOf(dto.getAmount()));
        transaction.setDescription(dto.getDescription());
        transaction.setDate(LocalDate.now());
        transaction.setType(category.getType());
        transaction.setCategory(category);

        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toViewDTO(saved);
    }

    private String getCurrentUserEmail() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return (String) jwtToken.getTokenAttributes().get("email");
        }
        throw new IllegalStateException("User not authenticated with JWT");
    }

    @Transactional
    public TransactionViewDTO createTransaction(CreateTransactionDTO createDto) {

        log.debug("Creating transaction for userId: {}", createDto.getUserId());

        Category category = categoryRepository.findById(createDto.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + createDto.getCategoryId()));

        transactionDomainService.validateTransactionCreation(
                createDto.getAmount(),
                createDto.getType(),
                category
        );

        Transaction transaction = transactionMapper.toEntity(createDto);
        transaction.setCategory(category);

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created with id: {}", savedTransaction.getId());
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

        transactionDomainService.validateMonthlyExpense(userId, year, month);

        return transactionRepository.calculateMonthlyExpense(userId, month, year);
    }

    @Transactional(readOnly = true)
    public String checkBudgetStatus(Long userId, BigDecimal budgetLimit) {

        transactionDomainService.validateBudgetCheckRequest(userId, budgetLimit);

        return transactionRepository.checkBudgetStatus(userId, budgetLimit);
    }

    @Transactional
    public Integer archiveOldTransactions(LocalDate cutoffDate) {

        log.info("Request to archive transactions older than: {}", cutoffDate);

        transactionDomainService.validateArchiveRequest(cutoffDate);

        Integer deletedCount = transactionRepository.archiveOldTransactions(cutoffDate);
        log.info("Archived {} transactions older than {}", deletedCount, cutoffDate);

        return deletedCount;
    }
}
