package com.example.microservice_ai.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.microservice_ai.client.AccountClient;
import com.example.microservice_ai.client.TransactionClient;
import com.example.microservice_ai.dto.AnalyzeTransactionsRequestDTO;
import com.example.microservice_ai.dto.AnalyzeTransactionsResponseDTO;
import com.example.microservice_ai.dto.CreateTransactionWithAIRequestDTO;
import com.example.microservice_ai.dto.CreateTransactionWithAIResponseDTO;
import com.example.microservice_ai.dto.external.CreateTransactionRequestDTO;
import com.example.microservice_ai.dto.external.TransactionResponseDTO;
import com.example.microservice_ai.entity.Message;
import com.example.microservice_ai.enums.Role;
import com.example.microservice_ai.exception.AccountNotFoundException;
import com.example.microservice_ai.exception.ServiceException;
import com.example.microservice_ai.service.CategorizationService;
import com.example.microservice_ai.service.IAIService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
public class AIController {
    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    private final CategorizationService categorizationService;

    @Autowired
    private IAIService aiService;

    @Autowired
    private AccountClient accountClient;

    @Autowired
    private TransactionClient transactionClient;

    public AIController(CategorizationService categorizationService) {
        this.categorizationService = categorizationService;
    }

    @PostMapping("/categorize")
    public ResponseEntity<String> categorizeTransaction(@RequestBody String description) {
        String category = categorizationService.categorize(description);
        return ResponseEntity.ok(category);
    }

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "AI response generated"),
        @ApiResponse(responseCode = "400", description = "Invalid prompt", content = @Content)
    })
    @GetMapping("/ask")
    public ResponseEntity<String> askAI(@RequestParam("prompt") String prompt) {
        logger.info("Received AI prompt request");
        logger.debug("Prompt content: {}", prompt);
        
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt cannot be empty");
        }
        
        Message message = new Message(Role.USER, prompt, null);
        String response = aiService.getAIResponseForMessage(message);
        logger.info("AI response generated successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Analyze user transactions with AI",
            description = "Fetches user transactions from microservice-transactions and provides AI-powered financial analysis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions analyzed successfully",
                content = @Content(schema = @Schema(implementation = AnalyzeTransactionsResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error communicating with other microservices", content = @Content)
    })
    @PostMapping("/analyze-transactions")
    public ResponseEntity<AnalyzeTransactionsResponseDTO> analyzeUserTransactions(
            @Valid @RequestBody AnalyzeTransactionsRequestDTO request) {
        logger.info("Analyzing transactions for email: {}", request.getEmail());

        Long userId = getUserIdOrThrow(request.getEmail());
        logger.debug("Retrieved userId: {} for email: {}", userId, request.getEmail());

        List<TransactionResponseDTO> transactions = getTransactionsOrEmpty(userId);
        logger.debug("Retrieved {} transactions for userId: {}", transactions.size(), userId);

        StringBuilder transactionSummary = new StringBuilder();
        transactionSummary.append("Analyze the following financial transactions and provide insights:\n\n");
        if (!transactions.isEmpty()) {
            for (TransactionResponseDTO tx : transactions) {
                transactionSummary.append(String.format("- %s: %s %s (Category: %s, Date: %s)\n",
                        tx.getType(), tx.getAmount(), tx.getDescription(),
                        tx.getCategoryName(), tx.getDate()));
            }
        } else {
            transactionSummary.append("No transactions found.\n");
        }

        if (request.getCustomPrompt() != null && !request.getCustomPrompt().isBlank()) {
            transactionSummary.append("\nAdditional request: ").append(request.getCustomPrompt());
        }

        Message aiMessage = new Message(Role.USER, transactionSummary.toString(), null);
        String aiAnalysis = aiService.getAIResponseForMessage(aiMessage);
        logger.info("AI analysis completed for user: {}", request.getEmail());

        AnalyzeTransactionsResponseDTO response = new AnalyzeTransactionsResponseDTO(
                request.getEmail(),
                userId,
                transactions.size(),
                transactions,
                aiAnalysis
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create transaction with AI-assisted type detection",
            description = "Creates a transaction using AI to detect whether it's INCOME or EXPENSE based on description")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Transaction created successfully",
                content = @Content(schema = @Schema(implementation = CreateTransactionWithAIResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error communicating with other microservices", content = @Content)
    })
    @PostMapping("/create-smart-transaction")
    public ResponseEntity<CreateTransactionWithAIResponseDTO> createSmartTransaction(
            @Valid @RequestBody CreateTransactionWithAIRequestDTO request) {
        logger.info("Creating smart transaction for email: {}", request.getEmail());

        Long userId = getUserIdOrThrow(request.getEmail());
        logger.debug("Retrieved userId: {} for email: {}", userId, request.getEmail());

        String typeDetectionPrompt = String.format(
                "Based on this transaction description, determine if it's an INCOME or EXPENSE. " +
                "Reply with ONLY the word 'INCOME' or 'EXPENSE', nothing else.\n\n" +
                "Description: %s\nAmount: %s",
                request.getDescription(), request.getAmount());

        Message typeMessage = new Message(Role.USER, typeDetectionPrompt, null);
        String detectedType = aiService.getAIResponseForMessage(typeMessage).trim().toUpperCase();

        if (!detectedType.equals("INCOME") && !detectedType.equals("EXPENSE")) {
            logger.warn("AI returned unclear type '{}', defaulting to EXPENSE", detectedType);
            detectedType = "EXPENSE";
        }
        logger.debug("AI detected transaction type: {}", detectedType);

        CreateTransactionRequestDTO transactionRequest = new CreateTransactionRequestDTO(
                userId,
                request.getAmount(),
                request.getDate() != null ? request.getDate() : LocalDate.now(),
                request.getDescription(),
                detectedType,
                request.getCategoryId()
        );

        TransactionResponseDTO createdTransaction = createTransactionOrThrow(transactionRequest);
        logger.info("Transaction created with id: {}", createdTransaction.getId());

        String suggestionPrompt = String.format(
                "Provide a brief, helpful financial tip (max 2 sentences) about this %s transaction:\n" +
                "Amount: %s\nDescription: %s",
                detectedType, request.getAmount(), request.getDescription());

        Message suggestionMessage = new Message(Role.USER, suggestionPrompt, null);
        String aiSuggestion = aiService.getAIResponseForMessage(suggestionMessage);

        CreateTransactionWithAIResponseDTO response = new CreateTransactionWithAIResponseDTO(
                createdTransaction,
                aiSuggestion,
                detectedType
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private Long getUserIdOrThrow(String email) {
        try {
            Long userId = accountClient.getUserIdByEmail(email).getBody();
            if (userId == null) {
                throw new AccountNotFoundException("User not found for email: " + email);
            }
            return userId;
        } catch (feign.FeignException.NotFound e) {
            throw new AccountNotFoundException("User not found for email: " + email);
        }
    }

    private List<TransactionResponseDTO> getTransactionsOrEmpty(Long userId) {
        try {
            List<TransactionResponseDTO> transactions = transactionClient.getTransactionsByUserId(userId).getBody();
            return transactions != null ? transactions : List.of();
        } catch (feign.FeignException e) {
            logger.warn("Failed to fetch transactions for userId {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    private TransactionResponseDTO createTransactionOrThrow(CreateTransactionRequestDTO request) {
        try {
            TransactionResponseDTO response = transactionClient.createTransaction(request).getBody();
            if (response == null) {
                throw new ServiceException("Failed to create transaction");
            }
            return response;
        } catch (feign.FeignException e) {
            throw new ServiceException("Transaction service error: " + e.getMessage());
        }
    }
}