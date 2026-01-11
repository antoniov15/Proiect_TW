package com.example.microservice_ai.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.example.microservice_ai.entity.Chat;
import com.example.microservice_ai.entity.Message;
import com.example.microservice_ai.enums.Role;
import com.example.microservice_ai.repository.MessageRepository;

/**
 * Mock tests for AIServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AIService Mock Tests")
class AIServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private RestTemplate restTemplate;

    private AIServiceImpl aiService;
    private Chat chat;

    @BeforeEach
    void setUp() {
        aiService = new AIServiceImpl(messageRepository);
        ReflectionTestUtils.setField(aiService, "aiApiUrl", "http://localhost:8089/ai");
        ReflectionTestUtils.setField(aiService, "restTemplate", restTemplate);
        
        chat = new Chat();
        chat.setTitle("Test Chat");
    }

    @Test
    @DisplayName("Should get AI response for single message")
    void testGetAIResponseForMessage() {
        Message message = new Message(Role.USER, "Hello AI", chat);
        when(restTemplate.postForObject(eq("http://localhost:8089/ai"), eq("Hello AI"), eq(String.class)))
            .thenReturn("AI Response");
        
        String result = aiService.getAIResponseForMessage(message);
        
        assertEquals("AI Response", result);
    }

    @Test
    @DisplayName("Should get AI response for conversation")
    void testGetAIResponseForConversation() {
        Message msg1 = new Message(Role.USER, "Hello", chat);
        Message msg2 = new Message(Role.ASSISTANT, "Hi", chat);
        List<Message> messages = List.of(msg1, msg2);
        
        when(restTemplate.postForObject(eq("http://localhost:8089/ai"), any(String.class), eq(String.class)))
            .thenReturn("Conversation response");
        
        String result = aiService.getAIResponseForConversation(messages);
        
        assertEquals("Conversation response", result);
    }
}
