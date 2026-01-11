package com.example.microservice_ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.microservice_ai.dto.ChatCreateDTO;
import com.example.microservice_ai.dto.ChatDTO;
import com.example.microservice_ai.dto.MessageCreateDTO;
import com.example.microservice_ai.dto.MessageDTO;
import com.example.microservice_ai.entity.Chat;
import com.example.microservice_ai.entity.Message;
import com.example.microservice_ai.enums.Role;
import com.example.microservice_ai.exception.AccountNotFoundException;
import com.example.microservice_ai.repository.ChatRepository;
import com.example.microservice_ai.repository.MessageRepository;

/**
 * Mock tests for ChatServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService Mock Tests")
class ChatServiceImplTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ChatServiceImpl chatService;

    private Chat chat;
    private Message message;

    @BeforeEach
    void setUp() {
        chat = new Chat();
        chat.setTitle("Test Chat");
        
        message = new Message(Role.USER, "Hello", chat);
    }

    @Test
    @DisplayName("Should create chat without messages")
    void testCreateChatWithoutMessages() {
        ChatCreateDTO dto = new ChatCreateDTO("New Chat", null);
        
        when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ChatDTO result = chatService.createChat(dto);
        
        assertNotNull(result);
        assertEquals("New Chat", result.getTitle());
        verify(chatRepository, times(1)).save(any(Chat.class));
    }

    @Test
    @DisplayName("Should create chat with messages")
    void testCreateChatWithMessages() {
        List<MessageCreateDTO> messages = new ArrayList<>();
        messages.add(new MessageCreateDTO("USER", "Hello"));
        ChatCreateDTO dto = new ChatCreateDTO("Chat with messages", messages);
        
        when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.findByChatIdOrderByCreatedAtAsc(any())).thenReturn(List.of(message));
        
        ChatDTO result = chatService.createChat(dto);
        
        assertNotNull(result);
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    @DisplayName("Should get chat by id")
    void testGetChat() {
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(messageRepository.findByChatIdOrderByCreatedAtAsc(any())).thenReturn(List.of(message));
        
        ChatDTO result = chatService.getChat(1L);
        
        assertNotNull(result);
        assertEquals("Test Chat", result.getTitle());
    }

    @Test
    @DisplayName("Should throw exception when chat not found")
    void testGetChatNotFound() {
        when(chatRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(AccountNotFoundException.class, () -> chatService.getChat(999L));
    }

    @Test
    @DisplayName("Should list all chats")
    void testListChats() {
        Chat chat2 = new Chat();
        chat2.setTitle("Chat 2");
        
        when(chatRepository.findAll()).thenReturn(List.of(chat, chat2));
        when(messageRepository.findByChatIdOrderByCreatedAtAsc(any())).thenReturn(new ArrayList<>());
        
        List<ChatDTO> result = chatService.listChats();
        
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should update chat title")
    void testUpdateChat() {
        ChatCreateDTO updateDto = new ChatCreateDTO("Updated Title", null);
        
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.findByChatIdOrderByCreatedAtAsc(any())).thenReturn(new ArrayList<>());
        
        ChatDTO result = chatService.updateChat(1L, updateDto);
        
        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    @DisplayName("Should delete chat and its messages")
    void testDeleteChat() {
        when(messageRepository.findByChatIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(message));
        
        chatService.deleteChat(1L);
        
        verify(messageRepository, times(1)).deleteAll(anyList());
        verify(chatRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should add message to chat")
    void testAddMessage() {
        MessageCreateDTO msgDto = new MessageCreateDTO("USER", "New message");
        
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        MessageDTO result = chatService.addMessage(1L, msgDto);
        
        assertNotNull(result);
        assertEquals("New message", result.getContent());
    }

    @Test
    @DisplayName("Should get messages for chat")
    void testGetMessagesForChat() {
        Message msg2 = new Message(Role.ASSISTANT, "Response", chat);
        
        when(chatRepository.existsById(1L)).thenReturn(true);
        when(messageRepository.findByChatIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(message, msg2));
        
        List<MessageDTO> result = chatService.getMessagesForChat(1L);
        
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should throw exception when getting messages for non-existent chat")
    void testGetMessagesForChatNotFound() {
        when(chatRepository.existsById(999L)).thenReturn(false);
        
        assertThrows(AccountNotFoundException.class, () -> chatService.getMessagesForChat(999L));
    }
}
