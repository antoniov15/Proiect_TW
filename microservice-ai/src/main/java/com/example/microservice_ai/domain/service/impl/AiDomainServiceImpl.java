package com.example.microservice_ai.domain.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import com.example.microservice_ai.domain.exception.AiServiceException;
import com.example.microservice_ai.domain.model.MessageModel;
import com.example.microservice_ai.domain.service.AiDomainService;

@Service
public class AiDomainServiceImpl implements AiDomainService {

    private final ChatModel chatModel;

    public AiDomainServiceImpl(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String generateResponse(List<MessageModel> conversationHistory) {
        try {
            List<org.springframework.ai.chat.messages.Message> aiMessages = new ArrayList<>();

            for (MessageModel msg : conversationHistory) {
                switch (msg.getRole()) {
                    case USER -> aiMessages.add(new UserMessage(msg.getContent()));
                    case ASSISTANT -> aiMessages.add(new AssistantMessage(msg.getContent()));
                    case CONTEXT -> aiMessages.add(new SystemMessage(msg.getContent()));
                }
            }

            Prompt prompt = new Prompt(aiMessages);
            return chatModel.call(prompt).getResult().getOutput().getContent();
        } catch (Exception e) {
            throw new AiServiceException("Failed to generate AI response", e);
        }
    }
}
