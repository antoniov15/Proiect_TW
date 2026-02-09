package com.example.microservice_ai.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.microservice_ai.domain.model.ChatAggregate;
import com.example.microservice_ai.domain.model.MessageModel;
import com.example.microservice_ai.domain.service.ChatDomainService;
import com.example.microservice_ai.dto.ChatCreateDTO;
import com.example.microservice_ai.dto.ChatDTO;
import com.example.microservice_ai.dto.MessageCreateDTO;
import com.example.microservice_ai.dto.MessageDTO;
import com.example.microservice_ai.enums.Role;
import com.example.microservice_ai.exception.AccountNotFoundException;

/**
 * Mock tests for ChatServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService Mock Tests")
class ChatServiceImplTest {

    @Mock
    private ChatDomainService chatDomainService;

    @InjectMocks
    private ChatServiceImpl chatService;

    private ChatAggregate chatAggregate;
    private MessageModel messageModel;

    @BeforeEach
    void setUp() {
        chatAggregate = new ChatAggregate(1L, "Test Chat", Instant.now());
        messageModel = new MessageModel(1L, Role.USER, "Hello", 1L, Instant.now());
    }

    @Test
    @DisplayName("Should create chat without messages")
    void testCreateChatWithoutMessages() {
        ChatCreateDTO dto = new ChatCreateDTO("New Chat", null);
        ChatAggregate newChat = new ChatAggregate(2L, "New Chat", Instant.now());
        
        when(chatDomainService.createChat("New Chat")).thenReturn(newChat);
        
        ChatDTO result = chatService.createChat(dto);
        
        assertNotNull(result);
        assertEquals("New Chat", result.getTitle());
        verify(chatDomainService, times(1)).createChat("New Chat");
    }

    @Test
    @DisplayName("Should create chat with messages")
    void testCreateChatWithMessages() {
        List<MessageCreateDTO> messages = new ArrayList<>();
        messages.add(new MessageCreateDTO("USER", "Hello"));
        ChatCreateDTO dto = new ChatCreateDTO("Chat with messages", messages);
        
        ChatAggregate newChat = new ChatAggregate(2L, "Chat with messages", Instant.now());
        ChatAggregate chatWithMessages = new ChatAggregate(2L, "Chat with messages", Instant.now());
        chatWithMessages.addMessage(messageModel);
        
        when(chatDomainService.createChat("Chat with messages")).thenReturn(newChat);
        when(chatDomainService.addMessageToChat(eq(2L), any(MessageModel.class))).thenReturn(messageModel);
        when(chatDomainService.getChatById(2L)).thenReturn(chatWithMessages);
        
        ChatDTO result = chatService.createChat(dto);
        
        assertNotNull(result);
        verify(chatDomainService, times(1)).addMessageToChat(eq(2L), any(MessageModel.class));
    }

    @Test
    @DisplayName("Should get chat by id")
    void testGetChat() {
        when(chatDomainService.getChatById(1L)).thenReturn(chatAggregate);
        
        ChatDTO result = chatService.getChat(1L);
        
        assertNotNull(result);
        assertEquals("Test Chat", result.getTitle());
    }

    @Test
    @DisplayName("Should throw exception when chat not found")
    void testGetChatNotFound() {
        when(chatDomainService.getChatById(999L)).thenThrow(new AccountNotFoundException("Chat not found"));
        
        assertThrows(AccountNotFoundException.class, () -> chatService.getChat(999L));
    }

    @Test
    @DisplayName("Should list all chats")
    void testListChats() {
        ChatAggregate chat2 = new ChatAggregate(2L, "Chat 2", Instant.now());
        
        when(chatDomainService.getAllChats()).thenReturn(List.of(chatAggregate, chat2));
        when(chatDomainService.getChatById(1L)).thenReturn(chatAggregate);
        when(chatDomainService.getChatById(2L)).thenReturn(chat2);
        
        List<ChatDTO> result = chatService.listChats();
        
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should update chat title")
    void testUpdateChat() {
        ChatCreateDTO updateDto = new ChatCreateDTO("Updated Title", null);
        ChatAggregate updatedChat = new ChatAggregate(1L, "Updated Title", Instant.now());
        
        when(chatDomainService.updateChat(1L, "Updated Title")).thenReturn(updatedChat);
        when(chatDomainService.getChatById(1L)).thenReturn(updatedChat);
        
        ChatDTO result = chatService.updateChat(1L, updateDto);
        
        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    @DisplayName("Should delete chat and its messages")
    void testDeleteChat() {
        doNothing().when(chatDomainService).deleteChat(1L);
        
        chatService.deleteChat(1L);
        
        verify(chatDomainService, times(1)).deleteChat(1L);
    }

    @Test
    @DisplayName("Should add message to chat")
    void testAddMessage() {
        MessageCreateDTO msgDto = new MessageCreateDTO("USER", "New message");
        MessageModel savedMessage = new MessageModel(2L, Role.USER, "New message", 1L, Instant.now());
        
        when(chatDomainService.addMessageToChat(eq(1L), any(MessageModel.class))).thenReturn(savedMessage);
        
        MessageDTO result = chatService.addMessage(1L, msgDto);
        
        assertNotNull(result);
        assertEquals("New message", result.getContent());
    }

    @Test
    @DisplayName("Should get messages for chat")
    void testGetMessagesForChat() {
        MessageModel msg2 = new MessageModel(2L, Role.ASSISTANT, "Response", 1L, Instant.now());
        
        when(chatDomainService.getChatMessages(1L)).thenReturn(List.of(messageModel, msg2));
        
        List<MessageDTO> result = chatService.getMessagesForChat(1L);
        
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should throw exception when getting messages for non-existent chat")
    void testGetMessagesForChatNotFound() {
        when(chatDomainService.getChatMessages(999L)).thenThrow(new AccountNotFoundException("Chat not found"));
        
        assertThrows(AccountNotFoundException.class, () -> chatService.getMessagesForChat(999L));
    }
}
