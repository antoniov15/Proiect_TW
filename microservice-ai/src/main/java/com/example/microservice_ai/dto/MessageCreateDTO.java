package com.example.microservice_ai.dto;

import jakarta.validation.constraints.NotBlank;

public class MessageCreateDTO {

    @NotBlank
    private String role;

    @NotBlank
    private String content;

    public MessageCreateDTO() {}

    public MessageCreateDTO(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role; }

    public void setRole(String role) {
        this.role = role; }

    public String getContent() {
        return content; }

    public void setContent(String content) {
        this.content = content; }
}
