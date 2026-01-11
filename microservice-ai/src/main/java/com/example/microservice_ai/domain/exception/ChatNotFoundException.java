package com.example.microservice_ai.domain.exception;

public class ChatNotFoundException extends RuntimeException {
    public ChatNotFoundException(Long id) {
        super("Chat not found with id: " + id);
    }
}
