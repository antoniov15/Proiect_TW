package com.example.microservice_ai.domain.service;

import java.util.List;

import com.example.microservice_ai.domain.model.ChatAggregate;
import com.example.microservice_ai.domain.model.MessageModel;

/**
 * Interfață domain pentru serviciul de gestionare a chat-urilor.
 * Definește operațiile de bază pentru gestionarea chat-urilor și mesajelor.
 * 
 * @author Daniel Ignat
 * @version 1.0
 * @since 2026-01-15
 */
public interface ChatDomainService {
    
    /**
     * Creează un chat nou cu titlul specificat.
     * 
     * @param title titlul chat-ului
     * @return chat-ul creat ca ChatAggregate
     * @author Daniel Ignat
     */
    ChatAggregate createChat(String title);
    
    /**
     * Obține un chat după identificatorul său.
     * 
     * @param id identificatorul unic al chat-ului
     * @return chat-ul găsit ca ChatAggregate
     * @author Daniel Ignat
     */
    ChatAggregate getChatById(Long id);
    
    /**
     * Obține toate chat-urile existente.
     * 
     * @return lista de chat-uri ca List de ChatAggregate
     * @author Daniel Ignat
     */
    List<ChatAggregate> getAllChats();
    
    /**
     * Actualizează titlul unui chat existent.
     * 
     * @param id identificatorul chat-ului de actualizat
     * @param title noul titlu pentru chat
     * @return chat-ul actualizat ca ChatAggregate
     * @author Daniel Ignat
     */
    ChatAggregate updateChat(Long id, String title);
    
    /**
     * Șterge un chat după identificatorul său.
     * 
     * @param id identificatorul chat-ului de șters
     * @author Daniel Ignat
     */
    void deleteChat(Long id);
    
    /**
     * Adaugă un mesaj la un chat existent.
     * 
     * @param chatId identificatorul chat-ului
     * @param message mesajul de adăugat
     * @return mesajul adăugat ca MessageModel
     * @author Daniel Ignat
     */
    MessageModel addMessageToChat(Long chatId, MessageModel message);
    
    /**
     * Obține toate mesajele dintr-un chat.
     * 
     * @param chatId identificatorul chat-ului
     * @return lista de mesaje ca List de MessageModel
     * @author Daniel Ignat
     */
    List<MessageModel> getChatMessages(Long chatId);
}
