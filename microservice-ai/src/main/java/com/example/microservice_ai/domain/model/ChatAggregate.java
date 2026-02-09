package com.example.microservice_ai.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatAggregate {
    private Long id;
    private String title;
    private Instant createdAt;
    private List<MessageModel> messages;

    public ChatAggregate() {
        this.messages = new ArrayList<>();
        this.createdAt = Instant.now();
    }

    public ChatAggregate(Long id, String title, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.messages = new ArrayList<>();
    }

    public void addMessage(MessageModel message) {
        this.messages.add(message);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<MessageModel> getMessages() { return Collections.unmodifiableList(messages); }
    public void setMessages(List<MessageModel> messages) { this.messages = new ArrayList<>(messages); }
}
