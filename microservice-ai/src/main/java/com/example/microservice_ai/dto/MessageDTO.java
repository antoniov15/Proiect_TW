package com.example.microservice_ai.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public class MessageDTO {
    private Long id;

    @NotBlank
    private String role;

    @NotBlank
    private String content;

    private Instant timestamp;

    public MessageDTO() {}

    public MessageDTO(Long id, String role, String content, Instant timestamp) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
