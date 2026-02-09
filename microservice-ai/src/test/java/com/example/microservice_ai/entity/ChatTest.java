package com.example.microservice_ai.entity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Chat entity.
 */
@DisplayName("Chat Entity Unit Tests")
class ChatTest {

    private Chat chat;

    @BeforeEach
    void setUp() {
        chat = new Chat();
    }

    @Test
    @DisplayName("Should create chat with default values")
    void testDefaultValues() {
        assertNull(chat.getId());
        assertNull(chat.getTitle());
        assertNotNull(chat.getCreatedAt());
    }

    @Test
    @DisplayName("Should set and get title correctly")
    void testSetAndGetTitle() {
        String title = "Test Chat";
        chat.setTitle(title);
        assertEquals(title, chat.getTitle());
    }

    @Test
    @DisplayName("Should have createdAt timestamp set on creation")
    void testCreatedAtIsSet() {
        Instant before = Instant.now().minusSeconds(1);
        Chat newChat = new Chat();
        Instant after = Instant.now().plusSeconds(1);

        assertTrue(newChat.getCreatedAt().isAfter(before));
        assertTrue(newChat.getCreatedAt().isBefore(after));
    }
}
