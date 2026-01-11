package com.example.microservice_ai.controller;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.microservice_ai.dto.ChatCreateDTO;
import com.example.microservice_ai.dto.ChatDTO;
import com.example.microservice_ai.dto.MessageCreateDTO;
import com.example.microservice_ai.dto.MessageDTO;
import com.example.microservice_ai.service.IChatService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * API tests for ChatController using MockMvc.
 */
@WebMvcTest(ChatController.class)
@DisplayName("ChatController API Tests")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    private ChatDTO chatDTO;
    private ChatCreateDTO createDTO;
    private MessageDTO messageDTO;

    @BeforeEach
    void setUp() {
        chatDTO = new ChatDTO(1L, "Test Chat", List.of(), Instant.now());
        createDTO = new ChatCreateDTO("Test Chat", null);
        messageDTO = new MessageDTO(1L, "USER", "Hello", Instant.now());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/chats - Should create chat")
    void testCreateChat() throws Exception {
        when(chatService.createChat(any(ChatCreateDTO.class))).thenReturn(chatDTO);

        mockMvc.perform(post("/api/chats")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Chat"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/chats - Should list all chats")
    void testListChats() throws Exception {
        when(chatService.listChats()).thenReturn(List.of(chatDTO));

        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/chats/{id} - Should get chat by id")
    void testGetChat() throws Exception {
        when(chatService.getChat(1L)).thenReturn(chatDTO);

        mockMvc.perform(get("/api/chats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/chats/{id} - Should delete chat")
    void testDeleteChat() throws Exception {
        doNothing().when(chatService).deleteChat(1L);

        mockMvc.perform(delete("/api/chats/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/chats/{chatId}/messages - Should add message")
    void testAddMessage() throws Exception {
        MessageCreateDTO msgCreate = new MessageCreateDTO("USER", "Hello");
        when(chatService.addMessage(eq(1L), any(MessageCreateDTO.class))).thenReturn(messageDTO);

        mockMvc.perform(post("/api/chats/1/messages")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(msgCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Hello"));
    }

    @Test
    @DisplayName("GET /api/chats - Should return 401 without authentication")
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isUnauthorized());
    }
}
