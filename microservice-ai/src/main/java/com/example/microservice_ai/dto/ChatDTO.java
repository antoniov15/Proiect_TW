package com.example.microservice_ai.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

public class ChatDTO {
    private Long id;

    @NotBlank
    private String title;

    private List<MessageDTO> messages;

    private Instant createdAt;

    public ChatDTO() {}

    public ChatDTO(Long id, String title, List<MessageDTO> messages, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.messages = messages;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id; }

    public void setId(Long id) {
        this.id = id; }

    public String getTitle() {
        return title; }

    public void setTitle(String title) {
        this.title = title; }

    public List<MessageDTO> getMessages() {
        return messages; }

    public void setMessages(List<MessageDTO> messages) {
        this.messages = messages; }

    public Instant getCreatedAt() {
        return createdAt; }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt; }
}

