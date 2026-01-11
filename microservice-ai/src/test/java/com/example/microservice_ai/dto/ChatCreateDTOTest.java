package com.example.microservice_ai.dto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ChatCreateDTO.
 */
@DisplayName("ChatCreateDTO Unit Tests")
class ChatCreateDTOTest {

    @Test
    @DisplayName("Should create ChatCreateDTO with default constructor")
    void testDefaultConstructor() {
        ChatCreateDTO dto = new ChatCreateDTO();
        assertNull(dto.getTitle());
        assertNull(dto.getMessages());
    }

    @Test
    @DisplayName("Should create ChatCreateDTO with parameterized constructor")
    void testParameterizedConstructor() {
        List<MessageCreateDTO> messages = new ArrayList<>();
        messages.add(new MessageCreateDTO("USER", "Hello"));
        ChatCreateDTO dto = new ChatCreateDTO("Test Title", messages);

        assertEquals("Test Title", dto.getTitle());
        assertEquals(1, dto.getMessages().size());
    }
}
