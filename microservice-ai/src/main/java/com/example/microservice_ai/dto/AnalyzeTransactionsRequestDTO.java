package com.example.microservice_ai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AnalyzeTransactionsRequestDTO {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    private String customPrompt;

    public AnalyzeTransactionsRequestDTO() {}

    public AnalyzeTransactionsRequestDTO(String email, String customPrompt) {
        this.email = email;
        this.customPrompt = customPrompt;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCustomPrompt() { return customPrompt; }
    public void setCustomPrompt(String customPrompt) { this.customPrompt = customPrompt; }
}
