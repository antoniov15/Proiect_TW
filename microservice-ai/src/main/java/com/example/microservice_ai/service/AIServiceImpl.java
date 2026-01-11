package com.example.microservice_ai.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.microservice_ai.entity.Message;
import com.example.microservice_ai.repository.MessageRepository;

@Service
public class AIServiceImpl implements IAIService {
    private static final Logger logger = LoggerFactory.getLogger(AIServiceImpl.class);

    private final ChatModel chatModel;
    private final MessageRepository messageRepository;

    @Autowired
    public AIServiceImpl(ChatModel chatModel, MessageRepository messageRepository) {
        this.chatModel = chatModel;
        this.messageRepository = messageRepository;
    }

    @Override
    public String getAIResponseForMessage(Message message) {
        logger.info("Processing AI request for single message");
        logger.debug("Message content length: {} characters", message.getContent() != null ? message.getContent().length() : 0);
        Prompt prompt = new Prompt(new UserMessage(message.getContent()));
        String response = chatModel.call(prompt).getResult().getOutput().getContent();
        logger.info("AI response generated successfully");
        logger.debug("Response length: {} characters", response != null ? response.length() : 0);
        return response;
    }

    @Override
    public String getAIResponseForConversation(List<Message> messages) {
        logger.info("Processing AI request for conversation with {} messages", messages.size());
        List<org.springframework.ai.chat.messages.Message> aiMessages = new ArrayList<>();
        
        for (Message msg : messages) {
            switch (msg.getRole()) {
                case USER -> {
                    aiMessages.add(new UserMessage(msg.getContent()));
                    logger.debug("Added USER message to conversation");
                }
                case ASSISTANT -> {
                    aiMessages.add(new AssistantMessage(msg.getContent()));
                    logger.debug("Added ASSISTANT message to conversation");
                }
                case CONTEXT -> {
                    aiMessages.add(new SystemMessage(msg.getContent()));
                    logger.debug("Added CONTEXT/SYSTEM message to conversation");
                }
            }
        }
        
        Prompt prompt = new Prompt(aiMessages);
        logger.debug("Sending prompt with {} messages to AI model", aiMessages.size());
        String response = chatModel.call(prompt).getResult().getOutput().getContent();
        logger.info("AI conversation response generated successfully");
        return response;
    }
}
