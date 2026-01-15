package com.example.microservice_ai.service;

import com.example.microservice_ai.entity.Message;
import java.util.List;

/**
 * Interfață pentru serviciul de inteligență artificială.
 * Definește operațiile disponibile pentru generarea răspunsurilor AI.
 * 
 * @author Daniel Ignat
 * @version 1.0
 * @since 2026-01-15
 */
public interface IAIService {
    
    /**
     * Generează un răspuns AI pentru un singur mesaj.
     * 
     * @param message mesajul pentru care se generează răspunsul
     * @return răspunsul generat de AI ca String
     * @author Daniel Ignat
     */
    String getAIResponseForMessage(Message message);
    
    /**
     * Generează un răspuns AI pentru o conversație completă.
     * Ia în considerare contextul tuturor mesajelor din conversație.
     * 
     * @param messages lista de mesaje din conversație
     * @return răspunsul generat de AI ca String
     * @author Daniel Ignat
     */
    String getAIResponseForConversation(List<Message> messages);
}
