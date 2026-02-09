package com.example.microservice_ai.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.microservice_ai.domain.service.AiDomainService;
import com.example.microservice_ai.entity.Chat;
import com.example.microservice_ai.entity.Message;
import com.example.microservice_ai.enums.Role;

@ExtendWith(MockitoExtension.class)
@DisplayName("AIService Mock Tests")
class AIServiceImplTest {

    @Mock
    private AiDomainService aiDomainService;

    private AIServiceImpl aiService;
    private Chat chat;

    @BeforeEach
    void setUp() {
        aiService = new AIServiceImpl(aiDomainService);
        
        chat = new Chat();
        chat.setTitle("Test Chat");
    }

    @Test
    @DisplayName("Should get AI response for single message")
    void testGetAIResponseForMessage() {
        Message message = new Message(Role.USER, "Hello AI", chat);
        
        when(aiDomainService.generateResponse(anyList())).thenReturn("AI Response");
        
        String result = aiService.getAIResponseForMessage(message);
        
        assertEquals("AI Response", result);
    }

    @Test
    @DisplayName("Should get AI response for conversation")
    void testGetAIResponseForConversation() {
        Message msg1 = new Message(Role.USER, "Hello", chat);
        Message msg2 = new Message(Role.ASSISTANT, "Hi", chat);
        List<Message> messages = List.of(msg1, msg2);
        
        when(aiDomainService.generateResponse(anyList())).thenReturn("Conversation response");
        
        String result = aiService.getAIResponseForConversation(messages);
        
        assertEquals("Conversation response", result);
    }
}
