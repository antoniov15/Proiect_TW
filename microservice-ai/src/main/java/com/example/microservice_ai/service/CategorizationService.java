package com.example.microservice_ai.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.microservice_ai.domain.model.MessageModel;
import com.example.microservice_ai.domain.service.AiDomainService;
import com.example.microservice_ai.enums.Role;

@Service
public class CategorizationService {
    private static final Logger logger = LoggerFactory.getLogger(CategorizationService.class);

    private static final String SYSTEM_PROMPT = """
        You are a financial transaction categorizer. Given a transaction description, 
        classify it into exactly ONE of these categories:
        - Food (restaurants, groceries, food delivery)
        - Transport (gas, uber, taxi, public transport, car services)
        - Entertainment (streaming services, games, movies, concerts)
        - Income (salary, wages, deposits, transfers received)
        - Health (pharmacy, doctor, hospital, medical)
        - Shopping (clothes, electronics, online purchases)
        - Bills (utilities, rent, subscriptions, phone, internet)
        - Travel (flights, hotels, vacation expenses)
        - Education (courses, books, tuition)
        - General (anything that doesn't fit other categories)
        
        Respond with ONLY the category name, nothing else.
        """;

    private final AiDomainService aiDomainService;

    public CategorizationService(AiDomainService aiDomainService) {
        this.aiDomainService = aiDomainService;
    }

    public String categorize(String description) {
        logger.info("AI categorization for: {}", description);

        if (description == null || description.isBlank()) {
            logger.debug("Empty description, returning General");
            return "General";
        }

        try {
            MessageModel systemMessage = new MessageModel(Role.CONTEXT, SYSTEM_PROMPT, null);
            MessageModel userMessage = new MessageModel(Role.USER, "Categorize this transaction: " + description, null);

            String response = aiDomainService.generateResponse(List.of(systemMessage, userMessage));

            String category = response.trim();
            logger.info("Transaction '{}' categorized as: {}", description, category);
            return category;

        } catch (Exception e) {
            logger.error("AI categorization failed for '{}': {}", description, e.getMessage());
            return fallbackCategorize(description);
        }
    }

    /**
     * Fallback keyword-based categorization in case AI service is unavailable
     */
    private String fallbackCategorize(String description) {
        logger.warn("Using fallback categorization for: {}", description);
        String text = description.toLowerCase();

        if (containsAny(text, "kfc", "mcdonalds", "pizza", "burger", "food", "restaurant", "grocery")) return "Food";
        if (containsAny(text, "uber", "bolt", "bus", "transport", "gas", "fuel", "taxi")) return "Transport";
        if (containsAny(text, "netflix", "spotify", "cinema", "game", "movie", "concert")) return "Entertainment";
        if (containsAny(text, "salary", "wage", "income", "deposit", "transfer")) return "Income";
        if (containsAny(text, "pharmacy", "doctor", "health", "hospital", "medical")) return "Health";
        if (containsAny(text, "amazon", "shop", "store", "purchase", "buy")) return "Shopping";
        if (containsAny(text, "electric", "water", "rent", "internet", "phone", "bill")) return "Bills";
        if (containsAny(text, "flight", "hotel", "airbnb", "vacation", "travel")) return "Travel";
        if (containsAny(text, "course", "book", "tuition", "school", "university")) return "Education";

        return "General";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}