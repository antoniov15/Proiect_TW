package com.example.microservice_ai.domain.repository.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.example.microservice_ai.domain.mapper.DomainMapper;
import com.example.microservice_ai.domain.model.ChatAggregate;
import com.example.microservice_ai.domain.repository.ChatDomainRepository;
import com.example.microservice_ai.entity.Chat;
import com.example.microservice_ai.repository.ChatRepository;

@Repository
public class ChatDomainRepositoryImpl implements ChatDomainRepository {

    private final ChatRepository chatRepository;

    public ChatDomainRepositoryImpl(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public ChatAggregate save(ChatAggregate chat) {
        Chat entity;
        if (chat.getId() != null) {
            // Update existing entity
            entity = chatRepository.findById(chat.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Chat not found: " + chat.getId()));
            DomainMapper.updateEntity(entity, chat);
        } else {
            // Create new entity
            entity = DomainMapper.toEntity(chat);
        }
        Chat saved = chatRepository.save(entity);
        return DomainMapper.toDomain(saved);
    }

    @Override
    public Optional<ChatAggregate> findById(Long id) {
        return chatRepository.findById(id).map(DomainMapper::toDomain);
    }

    @Override
    public List<ChatAggregate> findAll() {
        return chatRepository.findAll().stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        chatRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return chatRepository.existsById(id);
    }
}
