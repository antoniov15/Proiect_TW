package com.example.microservice_ai.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.microservice_ai.dto.MessageCreateDTO;
import com.example.microservice_ai.dto.MessageDTO;
import com.example.microservice_ai.entity.Chat;
import com.example.microservice_ai.entity.Message;
import com.example.microservice_ai.enums.Role;

/**
 * Unit tests for MessageMapper.
 */
@DisplayName("MessageMapper Unit Tests")
class MessageMapperTest {

    private Chat chat;

    @BeforeEach
    void setUp() {
        chat = new Chat();
        chat.setTitle("Test Chat");
    }

    @Test
    @DisplayName("Should convert MessageCreateDTO to Message entity with USER role")
    void testToEntityWithUserRole() {
        MessageCreateDTO dto = new MessageCreateDTO("USER", "Hello World");
        Message result = MessageMapper.toEntity(dto, chat);
        
        assertNotNull(result);
        assertEquals(Role.USER, result.getRole());
        assertEquals("Hello World", result.getContent());
        assertEquals(chat, result.getChat());
    }

    @Test
    @DisplayName("Should default to USER role for invalid role string")
    void testToEntityWithInvalidRole() {
        MessageCreateDTO dto = new MessageCreateDTO("INVALID_ROLE", "Content");
        Message result = MessageMapper.toEntity(dto, chat);
        
        assertEquals(Role.USER, result.getRole());
    }

    @Test
    @DisplayName("Should convert Message entity to MessageDTO")
    void testToDTO() {
        Message message = new Message(Role.USER, "Hello World", chat);
        MessageDTO result = MessageMapper.toDTO(message);
        
        assertNotNull(result);
        assertEquals("USER", result.getRole());
        assertEquals("Hello World", result.getContent());
    }
}
