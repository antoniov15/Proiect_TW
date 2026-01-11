package com.example.microservice_ai.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.microservice_ai.domain.model.MessageModel;
import com.example.microservice_ai.domain.service.AiDomainService;
import com.example.microservice_ai.entity.Message;

@Service
public class AIServiceImpl implements IAIService {
    private static final Logger logger = LoggerFactory.getLogger(AIServiceImpl.class);

    private final AiDomainService aiDomainService;

    public AIServiceImpl(AiDomainService aiDomainService) {
        this.aiDomainService = aiDomainService;
    }

    @Override
    public String getAIResponseForMessage(Message message) {
        logger.info("Processing AI request for single message");
        logger.debug("Message content length: {} characters", message.getContent() != null ? message.getContent().length() : 0);
        MessageModel model = new MessageModel(message.getRole(), message.getContent(), null);
        String response = aiDomainService.generateResponse(List.of(model));
        logger.info("AI response generated successfully");
        logger.debug("Response length: {} characters", response != null ? response.length() : 0);
        return response;
    }

    @Override
    public String getAIResponseForConversation(List<Message> messages) {
        logger.info("Processing AI request for conversation with {} messages", messages.size());
        List<MessageModel> models = messages.stream()
                .map(msg -> new MessageModel(msg.getRole(), msg.getContent(), 
                        msg.getChat() != null ? msg.getChat().getId() : null))
                .collect(Collectors.toList());
        String response = aiDomainService.generateResponse(models);
        logger.info("AI conversation response generated successfully");
        return response;
    }
}
