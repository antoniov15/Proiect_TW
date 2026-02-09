package com.example.microservice_ai.domain.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.microservice_ai.domain.exception.ChatNotFoundException;
import com.example.microservice_ai.domain.model.ChatAggregate;
import com.example.microservice_ai.domain.model.MessageModel;
import com.example.microservice_ai.domain.repository.ChatDomainRepository;
import com.example.microservice_ai.domain.repository.MessageDomainRepository;
import com.example.microservice_ai.domain.service.ChatDomainService;

@Service
public class ChatDomainServiceImpl implements ChatDomainService {

    private final ChatDomainRepository chatRepository;
    private final MessageDomainRepository messageRepository;

    public ChatDomainServiceImpl(ChatDomainRepository chatRepository, MessageDomainRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public ChatAggregate createChat(String title) {
        ChatAggregate chat = new ChatAggregate();
        chat.setTitle(title);
        return chatRepository.save(chat);
    }

    @Override
    public ChatAggregate getChatById(Long id) {
        ChatAggregate chat = chatRepository.findById(id)
                .orElseThrow(() -> new ChatNotFoundException(id));
        List<MessageModel> messages = messageRepository.findByChatId(id);
        chat.setMessages(messages);
        return chat;
    }

    @Override
    public List<ChatAggregate> getAllChats() {
        return chatRepository.findAll();
    }

    @Override
    public ChatAggregate updateChat(Long id, String title) {
        ChatAggregate chat = chatRepository.findById(id)
                .orElseThrow(() -> new ChatNotFoundException(id));
        chat.setTitle(title);
        return chatRepository.save(chat);
    }

    @Override
    public void deleteChat(Long id) {
        if (!chatRepository.existsById(id)) {
            throw new ChatNotFoundException(id);
        }
        messageRepository.deleteByChatId(id);
        chatRepository.deleteById(id);
    }

    @Override
    public MessageModel addMessageToChat(Long chatId, MessageModel message) {
        if (!chatRepository.existsById(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
        message.setChatId(chatId);
        return messageRepository.save(message);
    }

    @Override
    public List<MessageModel> getChatMessages(Long chatId) {
        if (!chatRepository.existsById(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
        return messageRepository.findByChatId(chatId);
    }
}
