package com.example.microservice_ai.controller;

import java.util.List;

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
		Message message = new Message(Role.USER, prompt, null);
		String response = aiService.getAIResponseForMessage(message);
		return ResponseEntity.ok(response);
	}

	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Chat created", content = @Content(schema = @Schema(implementation = ChatDTO.class))),
		@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
	})
	@PostMapping
	public ResponseEntity<ChatDTO> createChat(@Valid @RequestBody ChatCreateDTO dto) {
		ChatDTO created = chatService.createChat(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping
	public ResponseEntity<List<ChatDTO>> listChats() {
		return ResponseEntity.ok(chatService.listChats());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ChatDTO> getChat(@PathVariable("id") Long id) {
		return ResponseEntity.ok(chatService.getChat(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ChatDTO> updateChat(@PathVariable("id") Long id, @Valid @RequestBody ChatCreateDTO dto) {
		return ResponseEntity.ok(chatService.updateChat(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteChat(@PathVariable("id") Long id) {
		chatService.deleteChat(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{chatId}/messages")
	public ResponseEntity<MessageDTO> addMessage(@PathVariable("chatId") Long chatId, @Valid @RequestBody MessageCreateDTO dto) {
		MessageDTO created = chatService.addMessage(chatId, dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/{chatId}/messages")
	public ResponseEntity<List<MessageDTO>> getMessagesForChat(@PathVariable("chatId") Long chatId) {
		return ResponseEntity.ok(chatService.getMessagesForChat(chatId));
	}
}
