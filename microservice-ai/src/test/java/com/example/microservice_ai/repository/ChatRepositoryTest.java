package com.example.microservice_ai.repository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.microservice_ai.entity.Chat;

/**
 * Repository tests for ChatRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ChatRepository Tests")
class ChatRepositoryTest {

    @Autowired
    private ChatRepository chatRepository;

    @Test
    @DisplayName("Should save and find chat by id")
    void testSaveAndFindById() {
        Chat chat = new Chat();
        chat.setTitle("Test Chat");
        
        Chat saved = chatRepository.save(chat);
        
        assertNotNull(saved.getId());
        
        Optional<Chat> found = chatRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Chat", found.get().getTitle());
    }

    @Test
    @DisplayName("Should find all chats")
    void testFindAll() {
        Chat chat1 = new Chat();
        chat1.setTitle("Chat 1");
        Chat chat2 = new Chat();
        chat2.setTitle("Chat 2");
        
        chatRepository.save(chat1);
        chatRepository.save(chat2);
        
        List<Chat> chats = chatRepository.findAll();
        assertEquals(2, chats.size());
    }

    @Test
    @DisplayName("Should delete chat")
    void testDelete() {
        Chat chat = new Chat();
        chat.setTitle("To Delete");
        Chat saved = chatRepository.save(chat);
        
        chatRepository.deleteById(saved.getId());
        
        assertFalse(chatRepository.findById(saved.getId()).isPresent());
    }

    @Test
    @DisplayName("Should check existence by id")
    void testExistsById() {
        Chat chat = new Chat();
        chat.setTitle("Test");
        Chat saved = chatRepository.save(chat);
        
        assertTrue(chatRepository.existsById(saved.getId()));
        assertFalse(chatRepository.existsById(9999L));
    }
}
