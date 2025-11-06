package com.example.microservice_ai.enums;

/**
 * Represents the different roles in the chat system.
 * Each role has specific permissions and capabilities within the application.
 */
public enum Role {
    USER("user", "Regular user with standard chat capabilities"),
    ASSISTANT("assistant", "AI assistant with response capabilities"),
    CONTEXT("system", "System-level context and instructions provider");

    private final String code;
    private final String description;

    Role(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }

    public static Role fromCode(String code) {
        for (Role role : values()) {
            if (role.code.equalsIgnoreCase(code)) {
                return role;
            }
        }
        return null;
    }

    public boolean isAssistant() { return this == ASSISTANT; }
    public boolean isUser() { return this == USER; }
    public boolean isContext() { return this == CONTEXT; }
}
