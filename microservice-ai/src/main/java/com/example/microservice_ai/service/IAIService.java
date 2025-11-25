package com.example.microservice_ai.service;

import com.example.microservice_ai.entity.Message;
import java.util.List;

public interface IAIService {
    String getAIResponseForMessage(Message message);
    String getAIResponseForConversation(List<Message> messages);
}
