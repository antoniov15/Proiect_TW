package com.example.microservice_ai.service;

import com.example.microservice_ai.dto.ChatCreateDTO;
import com.example.microservice_ai.dto.ChatDTO;
import com.example.microservice_ai.dto.MessageCreateDTO;
import com.example.microservice_ai.dto.MessageDTO;
import java.util.List;

public interface IChatService {
    ChatDTO createChat(ChatCreateDTO chatCreateDTO);
    ChatDTO getChat(Long chatId);
    List<ChatDTO> listChats();
    ChatDTO updateChat(Long chatId, ChatCreateDTO chatCreateDTO);
    void deleteChat(Long chatId);

    MessageDTO addMessage(Long chatId, MessageCreateDTO messageCreateDTO);
    List<MessageDTO> getMessagesForChat(Long chatId);
}
