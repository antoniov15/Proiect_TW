package com.example.microservice_ai.domain.service;

import java.util.List;

import com.example.microservice_ai.domain.model.MessageModel;

/**
 * Interfață domain pentru serviciul de inteligență artificială.
 * Definește operațiile de bază pentru generarea răspunsurilor AI.
 * 
 * @author Daniel Ignat
 * @version 1.0
 * @since 2026-01-15
 */
public interface AiDomainService {
    
    /**
     * Generează un răspuns AI pe baza istoricului conversației.
     * 
     * @param conversationHistory lista de mesaje reprezentand istoricul conversației
     * @return răspunsul generat de AI ca String
     * @author Daniel Ignat
     */
    String generateResponse(List<MessageModel> conversationHistory);
}
