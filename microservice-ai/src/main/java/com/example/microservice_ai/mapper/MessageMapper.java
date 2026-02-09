package com.example.microservice_ai.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.microservice_ai.dto.MessageCreateDTO;
import com.example.microservice_ai.dto.MessageDTO;
import com.example.microservice_ai.entity.Chat;
import com.example.microservice_ai.entity.Message;
import com.example.microservice_ai.enums.Role;

public class MessageMapper {
	private static final Logger logger = LoggerFactory.getLogger(MessageMapper.class);

	public static Message toEntity(MessageCreateDTO dto, Chat chat) {
		Role roleEnum = null;
		try {
			roleEnum = Role.valueOf(dto.getRole().toUpperCase());
			logger.debug("Mapped role: {}", roleEnum);
		} catch (Exception e) {
			logger.warn("Invalid role '{}', defaulting to USER", dto.getRole());
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
