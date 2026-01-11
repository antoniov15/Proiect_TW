package com.example.microservice_ai.domain.model;

import java.time.Instant;

import com.example.microservice_ai.enums.Role;

public class MessageModel {
    private Long id;
    private Role role;
    private String content;
    private Long chatId;
    private Instant createdAt;

    public MessageModel() {
        this.createdAt = Instant.now();
    }

    public MessageModel(Role role, String content, Long chatId) {
        this.role = role;
        this.content = content;
        this.chatId = chatId;
        this.createdAt = Instant.now();
    }

    public MessageModel(Long id, Role role, String content, Long chatId, Instant createdAt) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.chatId = chatId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
