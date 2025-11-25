package com.example.microservice_ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.example.microservice_ai.dto.MessageCreateDTO;
import com.example.microservice_ai.dto.MessageDTO;
import com.example.microservice_ai.dto.ChatCreateDTO;
import com.example.microservice_ai.dto.ChatDTO;
import com.example.microservice_ai.service.IChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping(path="/chats")
public class ChatController {
	@Autowired
	private IChatService chatService;

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
