package com.example.microservice_ai.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements IChatService{
    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Override
    @Transactional
    public ChatDTO createChat(ChatCreateDTO chatCreateDTO) {
        Chat chat = ChatMapper.toEntity(chatCreateDTO);
        final Chat savedChat = chatRepository.save(chat);

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
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AccountNotFoundException("Chat not found: " + chatId));

        List<Message> messages = messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);

        return ChatMapper.toDTO(chat, messages.stream().map(MessageMapper::toDTO).collect(Collectors.toList()));
    }

    @Override
    public List<ChatDTO> listChats() {
        List<Chat> chats = chatRepository.findAll();

        return chats.stream().map(c -> {
            List<Message> messages = messageRepository.findByChatIdOrderByCreatedAtAsc(c.getId());

            return ChatMapper.toDTO(c, messages.stream().map(MessageMapper::toDTO).collect(Collectors.toList()));

        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatDTO updateChat(Long chatId, ChatCreateDTO chatCreateDTO) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AccountNotFoundException("Chat not found: " + chatId));

        if (chatCreateDTO.getTitle() != null) 
            chat.setTitle(chatCreateDTO.getTitle());

        chat = chatRepository.save(chat);

        List<Message> messages = messageRepository.findByChatIdOrderByCreatedAtAsc(chat.getId());

        return ChatMapper.toDTO(chat, messages.stream().map(MessageMapper::toDTO).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public void deleteChat(Long chatId) {
        List<Message> messages = messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);

        if (!messages.isEmpty()) {
            messageRepository.deleteAll(messages);
        }

        chatRepository.deleteById(chatId);
    }

    @Override
    @Transactional
    public MessageDTO addMessage(Long chatId, MessageCreateDTO messageCreateDTO) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AccountNotFoundException("Chat not found: " + chatId));

        Message message = MessageMapper.toEntity(messageCreateDTO, chat);

        message = messageRepository.save(message);

        return MessageMapper.toDTO(message);
    }

    @Override
    public List<MessageDTO> getMessagesForChat(Long chatId) {
        if (!chatRepository.existsById(chatId)) 
            throw new AccountNotFoundException("Chat not found: " + chatId);

        List<Message> messages = messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);

        return messages.stream().map(MessageMapper::toDTO).collect(Collectors.toList());
    }

}
