package com.example.microservice_ai.domain.repository;

import java.util.List;
import java.util.Optional;

import com.example.microservice_ai.domain.model.ChatAggregate;

public interface ChatDomainRepository {
    ChatAggregate save(ChatAggregate chat);
    Optional<ChatAggregate> findById(Long id);
    List<ChatAggregate> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
}
