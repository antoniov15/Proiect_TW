package com.example.microservice_ai.controller;

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

import com.example.microservice_ai.dto.ChatCreateDTO;
import com.example.microservice_ai.dto.ChatDTO;
import com.example.microservice_ai.dto.MessageCreateDTO;
import com.example.microservice_ai.dto.MessageDTO;
import com.example.microservice_ai.entity.Message;
import com.example.microservice_ai.enums.Role;
import com.example.microservice_ai.service.IAIService;
import com.example.microservice_ai.service.IChatService;

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
}
