package com.example.microservice_ai.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.microservice_ai.dto.ChatCreateDTO;
import com.example.microservice_ai.dto.ChatDTO;
import com.example.microservice_ai.dto.MessageCreateDTO;
import com.example.microservice_ai.dto.MessageDTO;
import com.example.microservice_ai.entity.Chat;
import com.example.microservice_ai.entity.Message;
import com.example.microservice_ai.exception.AccountNotFoundException;
import com.example.microservice_ai.mapper.ChatMapper;
import com.example.microservice_ai.mapper.MessageMapper;
import com.example.microservice_ai.repository.ChatRepository;
import com.example.microservice_ai.repository.MessageRepository;

@Service
public class ChatServiceImpl implements IChatService{
    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Override
    @Transactional
    public ChatDTO createChat(ChatCreateDTO chatCreateDTO) {
        logger.debug("Creating new chat with title: {}", chatCreateDTO.getTitle());
        Chat chat = ChatMapper.toEntity(chatCreateDTO);
        final Chat savedChat = chatRepository.save(chat);
        logger.info("Chat saved with id: {}", savedChat.getId());

        if (chatCreateDTO.getMessages() != null) {
            List<Message> messages = chatCreateDTO.getMessages().stream()
                    .map(m -> MessageMapper.toEntity(m, savedChat))
                    .map(messageRepository::save)
                    .collect(Collectors.toList());
            
            List<Message> persisted = messageRepository.findByChatIdOrderByCreatedAtAsc(savedChat.getId());
            
            return ChatMapper.toDTO(savedChat, persisted.stream().map(MessageMapper::toDTO).collect(Collectors.toList()));
        }

        return ChatMapper.toDTO(savedChat, List.of());
    }

    @Override
    public ChatDTO getChat(Long chatId) {
        logger.debug("Fetching chat with id: {}", chatId);
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> {
                    logger.warn("Chat not found with id: {}", chatId);
                    return new AccountNotFoundException("Chat not found: " + chatId);
                });

        List<Message> messages = messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);

        return ChatMapper.toDTO(chat, messages.stream().map(MessageMapper::toDTO).collect(Collectors.toList()));
    }

    @Override
    public List<ChatDTO> listChats() {
        logger.debug("Listing all chats");
        List<Chat> chats = chatRepository.findAll();
        logger.info("Found {} chats", chats.size());

        return chats.stream().map(c -> {
            List<Message> messages = messageRepository.findByChatIdOrderByCreatedAtAsc(c.getId());

            return ChatMapper.toDTO(c, messages.stream().map(MessageMapper::toDTO).collect(Collectors.toList()));

        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatDTO updateChat(Long chatId, ChatCreateDTO chatCreateDTO) {
        logger.debug("Updating chat with id: {}", chatId);
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> {
                    logger.warn("Chat not found for update with id: {}", chatId);
                    return new AccountNotFoundException("Chat not found: " + chatId);
                });

        if (chatCreateDTO.getTitle() != null) 
            chat.setTitle(chatCreateDTO.getTitle());

        chat = chatRepository.save(chat);
        logger.info("Chat updated successfully: {}", chatId);

        List<Message> messages = messageRepository.findByChatIdOrderByCreatedAtAsc(chat.getId());

        return ChatMapper.toDTO(chat, messages.stream().map(MessageMapper::toDTO).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public void deleteChat(Long chatId) {
        logger.debug("Deleting chat with id: {}", chatId);
        List<Message> messages = messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);

        if (!messages.isEmpty()) {
            logger.debug("Deleting {} messages for chat: {}", messages.size(), chatId);
            messageRepository.deleteAll(messages);
        }

        chatRepository.deleteById(chatId);
        logger.info("Chat deleted successfully: {}", chatId);
    }

    @Override
    @Transactional
    public MessageDTO addMessage(Long chatId, MessageCreateDTO messageCreateDTO) {
        logger.debug("Adding message to chat: {}", chatId);
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> {
                    logger.warn("Chat not found for adding message with id: {}", chatId);
                    return new AccountNotFoundException("Chat not found: " + chatId);
                });

        Message message = MessageMapper.toEntity(messageCreateDTO, chat);

        message = messageRepository.save(message);
        logger.info("Message added to chat {} with id: {}", chatId, message.getId());

        return MessageMapper.toDTO(message);
    }

    @Override
    public List<MessageDTO> getMessagesForChat(Long chatId) {
        logger.debug("Fetching messages for chat: {}", chatId);
        if (!chatRepository.existsById(chatId)) {
            logger.warn("Chat not found for fetching messages with id: {}", chatId);
            throw new AccountNotFoundException("Chat not found: " + chatId);
        }

        List<Message> messages = messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);
        logger.debug("Found {} messages for chat: {}", messages.size(), chatId);

        return messages.stream().map(MessageMapper::toDTO).collect(Collectors.toList());
    }

}
