package com.example.microservice_ai.mapper;

import com.example.microservice_ai.dto.ChatCreateDTO;
import com.example.microservice_ai.dto.ChatDTO;
import com.example.microservice_ai.dto.MessageDTO;
import com.example.microservice_ai.entity.Chat;
import java.util.List;

public class ChatMapper {

    public static Chat toEntity(ChatCreateDTO dto) {
        Chat chat = new Chat();
        chat.setTitle(dto.getTitle());
        return chat;
    }

    public static ChatDTO toDTO(Chat chat, List<MessageDTO> messages) {
        ChatDTO dto = new ChatDTO();
        dto.setId(chat.getId());
        dto.setTitle(chat.getTitle());
        dto.setCreatedAt(chat.getCreatedAt());
        dto.setMessages(messages);
        return dto;
    }
}
