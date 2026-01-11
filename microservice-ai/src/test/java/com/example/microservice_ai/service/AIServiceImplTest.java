package com.example.microservice_ai.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

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
    private ChatModel chatModel;

    @Mock
    private ChatResponse chatResponse;

    @Mock
    private Generation generation;

    private AIServiceImpl aiService;
    private Chat chat;

    @BeforeEach
    void setUp() {
        aiService = new AIServiceImpl(chatModel, messageRepository);
        
        chat = new Chat();
        chat.setTitle("Test Chat");
    }

    @Test
    @DisplayName("Should get AI response for single message")
    void testGetAIResponseForMessage() {
        Message message = new Message(Role.USER, "Hello AI", chat);
        
        AssistantMessage assistantMessage = new AssistantMessage("AI Response");
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        
        String result = aiService.getAIResponseForMessage(message);
        
        assertEquals("AI Response", result);
    }

    @Test
    @DisplayName("Should get AI response for conversation")
    void testGetAIResponseForConversation() {
        Message msg1 = new Message(Role.USER, "Hello", chat);
        Message msg2 = new Message(Role.ASSISTANT, "Hi", chat);
        List<Message> messages = List.of(msg1, msg2);
        
        AssistantMessage assistantMessage = new AssistantMessage("Conversation response");
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        
        String result = aiService.getAIResponseForConversation(messages);
        
        assertEquals("Conversation response", result);
    }
}
