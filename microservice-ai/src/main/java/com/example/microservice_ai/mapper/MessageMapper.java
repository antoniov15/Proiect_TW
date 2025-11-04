package com.example.microservice_ai.mapper;

import com.example.microservice_ai.dto.MessageCreateDTO;
import com.example.microservice_ai.dto.MessageDTO;
import com.example.microservice_ai.entity.Chat;
import com.example.microservice_ai.entity.Message;
import com.example.microservice_ai.enums.Role;

public class MessageMapper {

	public static Message toEntity(MessageCreateDTO dto, Chat chat) {
		Role roleEnum = null;
		try {
			roleEnum = Role.valueOf(dto.getRole().toUpperCase());
		} catch (Exception e) {
			// default to USER if unknown
			roleEnum = Role.USER;
		}
		Message m = new Message(roleEnum, dto.getContent(), chat);
		return m;
	}

	public static MessageDTO toDTO(Message message) {
		MessageDTO dto = new MessageDTO();
		dto.setId(message.getId());
		dto.setRole(message.getRole() != null ? message.getRole().name() : null);
		dto.setContent(message.getContent());
		dto.setTimestamp(message.getCreatedAt());
		return dto;
	}
}
