package com.example.microservice_ai.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.microservice_ai.enums.Role;

/**
 * Unit tests for the Message entity.
 */
@DisplayName("Message Entity Unit Tests")
class MessageTest {

    private Message message;
    private Chat chat;

    @BeforeEach
    void setUp() {
        chat = new Chat();
        chat.setTitle("Test Chat");
        message = new Message();
    }

    @Test
    @DisplayName("Should create message with default constructor")
    void testDefaultConstructor() {
        assertNull(message.getId());
        assertNull(message.getRole());
        assertNull(message.getContent());
        assertNull(message.getChat());
        assertNotNull(message.getCreatedAt());
    }

    @Test
    @DisplayName("Should create message with parameterized constructor")
    void testParameterizedConstructor() {
        Message msg = new Message(Role.USER, "Hello", chat);
        assertEquals(Role.USER, msg.getRole());
        assertEquals("Hello", msg.getContent());
        assertEquals(chat, msg.getChat());
    }

    @Test
    @DisplayName("Should set and get role correctly")
    void testSetAndGetRole() {
        message.setRole(Role.ASSISTANT);
        assertEquals(Role.ASSISTANT, message.getRole());
    }
}
