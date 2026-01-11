package com.example.microservice_ai.domain.repository.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.example.microservice_ai.domain.mapper.DomainMapper;
import com.example.microservice_ai.domain.model.MessageModel;
import com.example.microservice_ai.domain.repository.MessageDomainRepository;
import com.example.microservice_ai.entity.Chat;
import com.example.microservice_ai.entity.Message;
import com.example.microservice_ai.repository.ChatRepository;
import com.example.microservice_ai.repository.MessageRepository;

@Repository
public class MessageDomainRepositoryImpl implements MessageDomainRepository {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;

    public MessageDomainRepositoryImpl(MessageRepository messageRepository, ChatRepository chatRepository) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
    }

    @Override
    public MessageModel save(MessageModel message) {
        Chat chat = chatRepository.findById(message.getChatId()).orElse(null);
        Message entity = DomainMapper.toEntity(message, chat);
        Message saved = messageRepository.save(entity);
        return DomainMapper.toDomain(saved);
    }

    @Override
    public Optional<MessageModel> findById(Long id) {
        return messageRepository.findById(id).map(DomainMapper::toDomain);
    }

    @Override
    public List<MessageModel> findByChatId(Long chatId) {
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId).stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        messageRepository.deleteById(id);
    }

    @Override
    public void deleteByChatId(Long chatId) {
        List<Message> messages = messageRepository.findByChatId(chatId);
        messageRepository.deleteAll(messages);
    }
}
