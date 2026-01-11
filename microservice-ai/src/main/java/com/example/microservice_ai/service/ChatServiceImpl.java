package com.example.microservice_ai.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.microservice_ai.domain.model.ChatAggregate;
import com.example.microservice_ai.domain.model.MessageModel;
import com.example.microservice_ai.domain.service.ChatDomainService;
import com.example.microservice_ai.dto.ChatCreateDTO;
import com.example.microservice_ai.dto.ChatDTO;
import com.example.microservice_ai.dto.MessageCreateDTO;
import com.example.microservice_ai.dto.MessageDTO;
import com.example.microservice_ai.enums.Role;

@Service
public class ChatServiceImpl implements IChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatDomainService chatDomainService;

    public ChatServiceImpl(ChatDomainService chatDomainService) {
        this.chatDomainService = chatDomainService;
    }

    @Override
    @Transactional
    public ChatDTO createChat(ChatCreateDTO chatCreateDTO) {
        logger.debug("Creating new chat with title: {}", chatCreateDTO.getTitle());
        ChatAggregate chat = chatDomainService.createChat(chatCreateDTO.getTitle());
        logger.info("Chat saved with id: {}", chat.getId());

        if (chatCreateDTO.getMessages() != null && !chatCreateDTO.getMessages().isEmpty()) {
            for (MessageCreateDTO msgDto : chatCreateDTO.getMessages()) {
                MessageModel msg = toMessageModel(msgDto);
                chatDomainService.addMessageToChat(chat.getId(), msg);
            }
            chat = chatDomainService.getChatById(chat.getId());
        }

        return toChatDTO(chat);
    }

    @Override
    public ChatDTO getChat(Long chatId) {
        logger.debug("Fetching chat with id: {}", chatId);
        ChatAggregate chat = chatDomainService.getChatById(chatId);
        return toChatDTO(chat);
    }

    @Override
    public List<ChatDTO> listChats() {
        logger.debug("Listing all chats");
        List<ChatAggregate> chats = chatDomainService.getAllChats();
        logger.info("Found {} chats", chats.size());
        return chats.stream().map(c -> {
            ChatAggregate fullChat = chatDomainService.getChatById(c.getId());
            return toChatDTO(fullChat);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatDTO updateChat(Long chatId, ChatCreateDTO chatCreateDTO) {
        logger.debug("Updating chat with id: {}", chatId);
        ChatAggregate chat = chatDomainService.updateChat(chatId, chatCreateDTO.getTitle());
        logger.info("Chat updated successfully: {}", chatId);
        return toChatDTO(chatDomainService.getChatById(chatId));
    }

    @Override
    @Transactional
    public void deleteChat(Long chatId) {
        logger.debug("Deleting chat with id: {}", chatId);
        chatDomainService.deleteChat(chatId);
        logger.info("Chat deleted successfully: {}", chatId);
    }

    @Override
    @Transactional
    public MessageDTO addMessage(Long chatId, MessageCreateDTO messageCreateDTO) {
        logger.debug("Adding message to chat: {}", chatId);
        MessageModel message = toMessageModel(messageCreateDTO);
        MessageModel saved = chatDomainService.addMessageToChat(chatId, message);
        logger.info("Message added to chat {} with id: {}", chatId, saved.getId());
        return toMessageDTO(saved);
    }

    @Override
    public List<MessageDTO> getMessagesForChat(Long chatId) {
        logger.debug("Fetching messages for chat: {}", chatId);
        List<MessageModel> messages = chatDomainService.getChatMessages(chatId);
        logger.debug("Found {} messages for chat: {}", messages.size(), chatId);
        return messages.stream().map(this::toMessageDTO).collect(Collectors.toList());
    }

    private ChatDTO toChatDTO(ChatAggregate chat) {
        ChatDTO dto = new ChatDTO();
        dto.setId(chat.getId());
        dto.setTitle(chat.getTitle());
        dto.setCreatedAt(chat.getCreatedAt());
        dto.setMessages(chat.getMessages().stream().map(this::toMessageDTO).collect(Collectors.toList()));
        return dto;
    }

    private MessageDTO toMessageDTO(MessageModel msg) {
        MessageDTO dto = new MessageDTO();
        dto.setId(msg.getId());
        dto.setRole(msg.getRole() != null ? msg.getRole().name() : null);
        dto.setContent(msg.getContent());
        dto.setTimestamp(msg.getCreatedAt());
        return dto;
    }

    private MessageModel toMessageModel(MessageCreateDTO dto) {
        Role role = Role.USER;
        try {
            role = Role.valueOf(dto.getRole().toUpperCase());
        } catch (Exception e) {
            logger.warn("Invalid role '{}', defaulting to USER", dto.getRole());
        }
        return new MessageModel(role, dto.getContent(), null);
    }
}
