package com.example.microservice_ai.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class ChatCreateDTO {

    @NotBlank
    private String title;

    private List<MessageCreateDTO> messages;

    public ChatCreateDTO() {}

    public ChatCreateDTO(String title, List<MessageCreateDTO> messages) {
        this.title = title;
        this.messages = messages;
    }

    public String getTitle() {
        return title; }

    public void setTitle(String title) {
        this.title = title; }

    public List<MessageCreateDTO> getMessages() {
        return messages; }

    public void setMessages(List<MessageCreateDTO> messages) {
        this.messages = messages; }
}

