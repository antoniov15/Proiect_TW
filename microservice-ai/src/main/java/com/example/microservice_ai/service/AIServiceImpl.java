package com.example.microservice_ai.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.microservice_ai.domain.model.MessageModel;
import com.example.microservice_ai.domain.service.AiDomainService;
import com.example.microservice_ai.entity.Message;

/**
 * Implementare a serviciului de inteligență artificială.
 * Gestionează comunicarea cu serviciul AI pentru generarea răspunsurilor.
 * 
 * @author Daniel Ignat
 * @version 1.0
 * @since 2026-01-15
 * @see IAIService
 * @see AiDomainService
 */
@Service
public class AIServiceImpl implements IAIService {
    private static final Logger logger = LoggerFactory.getLogger(AIServiceImpl.class);

    private final AiDomainService aiDomainService;

    /**
     * Constructor pentru AIServiceImpl.
     * 
     * @param aiDomainService serviciul domain pentru AI
     * @author Daniel Ignat
     */
    public AIServiceImpl(AiDomainService aiDomainService) {
        this.aiDomainService = aiDomainService;
    }

    /**
     * {@inheritDoc}
     * Procesează un singur mesaj și returnează răspunsul AI.
     * 
     * @param message mesajul de procesat
     * @return răspunsul generat de AI
     * @author Daniel Ignat
     */
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

    /**
     * {@inheritDoc}
     * Procesează o conversație completă și returnează răspunsul AI.
     * Convertește mesajele în modele și le trimite către serviciul domain.
     * 
     * @param messages lista de mesaje din conversație
     * @return răspunsul generat de AI pentru întreaga conversație
     * @author Daniel Ignat
     */
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
