package com.example.microservice_ai.service;

import com.example.microservice_ai.dto.ChatCreateDTO;
import com.example.microservice_ai.dto.ChatDTO;
import com.example.microservice_ai.dto.MessageCreateDTO;
import com.example.microservice_ai.dto.MessageDTO;
import java.util.List;

/**
 * Interfață pentru serviciul de gestionare a chat-urilor.
 * Definește operațiile CRUD pentru chat-uri și mesaje.
 * 
 * @author Daniel Ignat
 * @version 1.0
 * @since 2026-01-15
 */
public interface IChatService {
    
    /**
     * Creează un chat nou.
     * 
     * @param chatCreateDTO datele pentru crearea chat-ului
     * @return chat-ul creat ca ChatDTO
     * @author Daniel Ignat
     */
    ChatDTO createChat(ChatCreateDTO chatCreateDTO);
    
    /**
     * Obține un chat după ID.
     * 
     * @param chatId identificatorul unic al chat-ului
     * @return chat-ul găsit ca ChatDTO
     * @author Daniel Ignat
     */
    ChatDTO getChat(Long chatId);
    
    /**
     * Listează toate chat-urile existente.
     * 
     * @return lista de chat-uri ca List de ChatDTO
     * @author Daniel Ignat
     */
    List<ChatDTO> listChats();
    
    /**
     * Actualizează un chat existent.
     * 
     * @param chatId identificatorul chat-ului de actualizat
     * @param chatCreateDTO datele noi pentru chat
     * @return chat-ul actualizat ca ChatDTO
     * @author Daniel Ignat
     */
    ChatDTO updateChat(Long chatId, ChatCreateDTO chatCreateDTO);
    
    /**
     * Șterge un chat după ID.
     * 
     * @param chatId identificatorul chat-ului de șters
     * @author Daniel Ignat
     */
    void deleteChat(Long chatId);

    /**
     * Adaugă un mesaj la un chat existent.
     * 
     * @param chatId identificatorul chat-ului
     * @param messageCreateDTO datele mesajului de adăugat
     * @return mesajul adăugat ca MessageDTO
     * @author Daniel Ignat
     */
    MessageDTO addMessage(Long chatId, MessageCreateDTO messageCreateDTO);
    
    /**
     * Obține toate mesajele dintr-un chat.
     * 
     * @param chatId identificatorul chat-ului
     * @return lista de mesaje ca List de MessageDTO
     * @author Daniel Ignat
     */
    List<MessageDTO> getMessagesForChat(Long chatId);
}
