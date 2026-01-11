package com.example.microservice_ai.service;

import com.example.microservice_ai.entity.Message;
import com.example.microservice_ai.repository.MessageRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AIServiceImpl implements IAIService {
    private final ChatModel chatModel;
    private final MessageRepository messageRepository;

    @Autowired
    public AIServiceImpl(ChatModel chatModel, MessageRepository messageRepository) {
        this.chatModel = chatModel;
        this.messageRepository = messageRepository;
    }

    @Override
    public String getAIResponseForMessage(Message message) {
        Prompt prompt = new Prompt(new UserMessage(message.getContent()));
        return chatModel.call(prompt).getResult().getOutput().getContent();
    }

    @Override
    public String getAIResponseForConversation(List<Message> messages) {
        List<org.springframework.ai.chat.messages.Message> aiMessages = new ArrayList<>();
        
        for (Message msg : messages) {
            switch (msg.getRole()) {
                case USER -> aiMessages.add(new UserMessage(msg.getContent()));
                case ASSISTANT -> aiMessages.add(new AssistantMessage(msg.getContent()));
                case CONTEXT -> aiMessages.add(new SystemMessage(msg.getContent()));
            }
        }
        
        Prompt prompt = new Prompt(aiMessages);
        return chatModel.call(prompt).getResult().getOutput().getContent();
    }
}
