package com.example.microservice_ai.domain.repository;

import java.util.List;
import java.util.Optional;

import com.example.microservice_ai.domain.model.MessageModel;

public interface MessageDomainRepository {
    MessageModel save(MessageModel message);
    Optional<MessageModel> findById(Long id);
    List<MessageModel> findByChatId(Long chatId);
    void deleteById(Long id);
    void deleteByChatId(Long chatId);
}
