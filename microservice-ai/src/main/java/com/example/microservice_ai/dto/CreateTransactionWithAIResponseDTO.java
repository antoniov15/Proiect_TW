package com.example.microservice_ai.dto;

import com.example.microservice_ai.dto.external.TransactionResponseDTO;

public class CreateTransactionWithAIResponseDTO {
    private TransactionResponseDTO transaction;
    private String aiSuggestion; // AI-generated suggestion about the transaction
    private String detectedType; // AI-detected transaction type (INCOME/EXPENSE)

    public CreateTransactionWithAIResponseDTO() {}

    public CreateTransactionWithAIResponseDTO(TransactionResponseDTO transaction, String aiSuggestion, String detectedType) {
        this.transaction = transaction;
        this.aiSuggestion = aiSuggestion;
        this.detectedType = detectedType;
    }

    public TransactionResponseDTO getTransaction() { return transaction; }
    public void setTransaction(TransactionResponseDTO transaction) { this.transaction = transaction; }

    public String getAiSuggestion() { return aiSuggestion; }
    public void setAiSuggestion(String aiSuggestion) { this.aiSuggestion = aiSuggestion; }

    public String getDetectedType() { return detectedType; }
    public void setDetectedType(String detectedType) { this.detectedType = detectedType; }
}
