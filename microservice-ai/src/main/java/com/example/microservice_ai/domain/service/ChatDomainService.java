package com.example.microservice_ai.domain.service;

import java.util.List;

import com.example.microservice_ai.domain.model.ChatAggregate;
import com.example.microservice_ai.domain.model.MessageModel;

public interface ChatDomainService {
    ChatAggregate createChat(String title);
    ChatAggregate getChatById(Long id);
    List<ChatAggregate> getAllChats();
    ChatAggregate updateChat(Long id, String title);
    void deleteChat(Long id);
    MessageModel addMessageToChat(Long chatId, MessageModel message);
    List<MessageModel> getChatMessages(Long chatId);
}
