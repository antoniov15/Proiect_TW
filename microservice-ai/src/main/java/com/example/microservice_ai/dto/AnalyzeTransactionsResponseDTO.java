package com.example.microservice_ai.dto;

import java.util.List;

import com.example.microservice_ai.dto.external.TransactionResponseDTO;

public class AnalyzeTransactionsResponseDTO {
    private String email;
    private Long userId;
    private int transactionCount;
    private List<TransactionResponseDTO> transactions;
    private String aiAnalysis;

    public AnalyzeTransactionsResponseDTO() {}

    public AnalyzeTransactionsResponseDTO(String email, Long userId, int transactionCount, List<TransactionResponseDTO> transactions, String aiAnalysis) {
        this.email = email;
        this.userId = userId;
        this.transactionCount = transactionCount;
        this.transactions = transactions;
        this.aiAnalysis = aiAnalysis;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }

    public List<TransactionResponseDTO> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionResponseDTO> transactions) { this.transactions = transactions; }

    public String getAiAnalysis() { return aiAnalysis; }
    public void setAiAnalysis(String aiAnalysis) { this.aiAnalysis = aiAnalysis; }
}
