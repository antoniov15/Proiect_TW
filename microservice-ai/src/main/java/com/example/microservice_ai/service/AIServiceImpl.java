package com.example.microservice_ai.service;

import com.example.microservice_ai.entity.Message;
import com.example.microservice_ai.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;

@Service
public class AIServiceImpl implements IAIService {
    private final RestTemplate restTemplate;
    private final MessageRepository messageRepository;
    @Value("${ai.api.url}")
    private String aiApiUrl;

    @Autowired
    public AIServiceImpl(MessageRepository messageRepository) {
        this.restTemplate = new RestTemplate();
        this.messageRepository = messageRepository;
    }

    @Override
    public String getAIResponseForMessage(Message message) {
           String payload = message.getContent();
           return restTemplate.postForObject(aiApiUrl, payload, String.class);
    }

    @Override
    public String getAIResponseForConversation(List<Message> messages) {
            StringBuilder conversation = new StringBuilder();
            for (Message msg : messages) {
                conversation.append(msg.getContent()).append("\n");
            }
            return restTemplate.postForObject(aiApiUrl, conversation.toString(), String.class);
    }
}
