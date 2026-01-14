package com.example.microservice_ai.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.microservice_ai.client.AccountClient;
import com.example.microservice_ai.client.TransactionClient;
import com.example.microservice_ai.dto.AnalyzeTransactionsRequestDTO;
import com.example.microservice_ai.dto.AnalyzeTransactionsResponseDTO;
import com.example.microservice_ai.dto.ChatCreateDTO;
import com.example.microservice_ai.dto.ChatDTO;
import com.example.microservice_ai.dto.CreateTransactionWithAIRequestDTO;
import com.example.microservice_ai.dto.CreateTransactionWithAIResponseDTO;
import com.example.microservice_ai.dto.MessageCreateDTO;
import com.example.microservice_ai.dto.MessageDTO;
import com.example.microservice_ai.dto.external.CreateTransactionRequestDTO;
import com.example.microservice_ai.dto.external.TransactionResponseDTO;
import com.example.microservice_ai.entity.Message;
import com.example.microservice_ai.enums.Role;
import com.example.microservice_ai.service.IAIService;
import com.example.microservice_ai.service.IChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path="/api/chats")
public class ChatController {
	private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

	@Autowired
	private IChatService chatService;

	@Autowired
	private IAIService aiService;

	@Autowired
	private AccountClient accountClient;

	@Autowired
	private TransactionClient transactionClient;

	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "AI response generated"),
		@ApiResponse(responseCode = "400", description = "Invalid prompt", content = @Content)
	})
	@GetMapping("/ask")
	public ResponseEntity<String> askAI(@org.springframework.web.bind.annotation.RequestParam("prompt") String prompt) {
		logger.info("Received AI prompt request");
		logger.debug("Prompt content: {}", prompt);
		Message message = new Message(Role.USER, prompt, null);
		String response = aiService.getAIResponseForMessage(message);
		logger.info("AI response generated successfully");
		return ResponseEntity.ok(response);
	}

	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Chat created", content = @Content(schema = @Schema(implementation = ChatDTO.class))),
		@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
	})
	@PostMapping
	public ResponseEntity<ChatDTO> createChat(@Valid @RequestBody ChatCreateDTO dto) {
		logger.info("Creating new chat with title: {}", dto.getTitle());
		ChatDTO created = chatService.createChat(dto);
		logger.info("Chat created successfully with id: {}", created.getId());
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping
	public ResponseEntity<List<ChatDTO>> listChats() {
		logger.info("Fetching all chats");
		List<ChatDTO> chats = chatService.listChats();
		logger.debug("Retrieved {} chats", chats.size());
		return ResponseEntity.ok(chats);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ChatDTO> getChat(@PathVariable("id") Long id) {
		logger.info("Fetching chat with id: {}", id);
		ChatDTO chat = chatService.getChat(id);
		logger.debug("Chat retrieved: {}", chat.getTitle());
		return ResponseEntity.ok(chat);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ChatDTO> updateChat(@PathVariable("id") Long id, @Valid @RequestBody ChatCreateDTO dto) {
		logger.info("Updating chat with id: {}", id);
		ChatDTO updated = chatService.updateChat(id, dto);
		logger.info("Chat updated successfully: {}", updated.getTitle());
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteChat(@PathVariable("id") Long id) {
		logger.info("Deleting chat with id: {}", id);
		chatService.deleteChat(id);
		logger.info("Chat deleted successfully: {}", id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{chatId}/messages")
	public ResponseEntity<MessageDTO> addMessage(@PathVariable("chatId") Long chatId, @Valid @RequestBody MessageCreateDTO dto) {
		logger.info("Adding message to chat: {}", chatId);
		logger.debug("Message role: {}", dto.getRole());
		MessageDTO created = chatService.addMessage(chatId, dto);
		logger.info("Message added successfully with id: {}", created.getId());
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/{chatId}/messages")
	public ResponseEntity<List<MessageDTO>> getMessagesForChat(@PathVariable("chatId") Long chatId) {
		logger.info("Fetching messages for chat: {}", chatId);
		List<MessageDTO> messages = chatService.getMessagesForChat(chatId);
		logger.debug("Retrieved {} messages for chat: {}", messages.size(), chatId);
		return ResponseEntity.ok(messages);
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

		Long userId = accountClient.getUserIdByEmail(request.getEmail()).getBody();
		logger.debug("Retrieved userId: {} for email: {}", userId, request.getEmail());

		List<TransactionResponseDTO> transactions = transactionClient.getTransactionsByUserId(userId).getBody();
		logger.debug("Retrieved {} transactions for userId: {}", transactions != null ? transactions.size() : 0, userId);

		StringBuilder transactionSummary = new StringBuilder();
		transactionSummary.append("Analyze the following financial transactions and provide insights:\n\n");
		if (transactions != null && !transactions.isEmpty()) {
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
				transactions != null ? transactions.size() : 0,
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

		Long userId = accountClient.getUserIdByEmail(request.getEmail()).getBody();
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

		TransactionResponseDTO createdTransaction = transactionClient.createTransaction(transactionRequest).getBody();
		logger.info("Transaction created with id: {}", createdTransaction != null ? createdTransaction.getId() : "null");

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
}
