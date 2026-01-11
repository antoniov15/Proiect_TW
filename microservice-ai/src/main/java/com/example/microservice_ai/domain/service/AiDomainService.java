package com.example.microservice_ai.domain.service;

import java.util.List;

import com.example.microservice_ai.domain.model.MessageModel;

public interface AiDomainService {
    String generateResponse(List<MessageModel> conversationHistory);
}
