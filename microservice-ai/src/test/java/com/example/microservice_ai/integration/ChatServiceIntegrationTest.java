package com.example.microservice_ai.integration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.microservice_ai.dto.ChatCreateDTO;
import com.example.microservice_ai.dto.ChatDTO;
import com.example.microservice_ai.dto.MessageCreateDTO;
import com.example.microservice_ai.repository.ChatRepository;
import com.example.microservice_ai.repository.MessageRepository;
import com.example.microservice_ai.service.IChatService;

/**
 * Integration tests for ChatService with real database.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ChatService Integration Tests")
class ChatServiceIntegrationTest {

    @Autowired
    private IChatService chatService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        chatRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create chat and persist to database")
    void testCreateChatIntegration() {
        ChatCreateDTO createDTO = new ChatCreateDTO("Integration Test Chat", null);
        
        ChatDTO result = chatService.createChat(createDTO);
        
        assertNotNull(result.getId());
        assertTrue(chatRepository.existsById(result.getId()));
    }

    @Test
    @DisplayName("Should create chat with messages")
    void testCreateChatWithMessagesIntegration() {
        List<MessageCreateDTO> messages = new ArrayList<>();
        messages.add(new MessageCreateDTO("USER", "Hello"));
        messages.add(new MessageCreateDTO("ASSISTANT", "Hi there"));
        ChatCreateDTO createDTO = new ChatCreateDTO("Chat with Messages", messages);
        
        ChatDTO result = chatService.createChat(createDTO);
        
        assertNotNull(result.getId());
        assertEquals(2, messageRepository.findByChatId(result.getId()).size());
    }

    @Test
    @DisplayName("Should list all chats from database")
    void testListChatsIntegration() {
        chatService.createChat(new ChatCreateDTO("Chat 1", null));
        chatService.createChat(new ChatCreateDTO("Chat 2", null));
        
        List<ChatDTO> chats = chatService.listChats();
        
        assertEquals(2, chats.size());
    }

    @Test
    @DisplayName("Should delete chat and its messages from database")
    void testDeleteChatIntegration() {
        List<MessageCreateDTO> messages = new ArrayList<>();
        messages.add(new MessageCreateDTO("USER", "Message"));
        ChatDTO created = chatService.createChat(new ChatCreateDTO("To Delete", messages));
        
        chatService.deleteChat(created.getId());
        
        assertFalse(chatRepository.existsById(created.getId()));
        assertTrue(messageRepository.findByChatId(created.getId()).isEmpty());
    }

    @Test
    @DisplayName("Full workflow: create, update, add messages, delete")
    void testFullWorkflowIntegration() {
        // Create
        ChatDTO chat = chatService.createChat(new ChatCreateDTO("Workflow Test", null));
        assertNotNull(chat.getId());
        
        // Update
        ChatDTO updated = chatService.updateChat(chat.getId(), new ChatCreateDTO("Updated Workflow", null));
        assertEquals("Updated Workflow", updated.getTitle());
        
        // Add messages
        chatService.addMessage(chat.getId(), new MessageCreateDTO("USER", "Question"));
        chatService.addMessage(chat.getId(), new MessageCreateDTO("ASSISTANT", "Answer"));
        assertEquals(2, chatService.getMessagesForChat(chat.getId()).size());
        
        // Delete
        chatService.deleteChat(chat.getId());
        assertFalse(chatRepository.existsById(chat.getId()));
    }
}
