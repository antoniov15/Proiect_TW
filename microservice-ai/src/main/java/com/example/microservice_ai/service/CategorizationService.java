package com.example.microservice_ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
public class CategorizationService {
    private static final Logger logger = LoggerFactory.getLogger(CategorizationService.class);

    public String categorize(String description) {
        logger.info("Simulating AI categorization for: {}", description);

        if (description == null) return "General";
        String text = description.toLowerCase(Locale.ROOT);

        if (containsAny(text, "kfc", "mcdonalds", "pizza", "burger", "food")) return "Food";
        if (containsAny(text, "uber", "bolt", "bus", "transport", "gas")) return "Transport";
        if (containsAny(text, "netflix", "spotify", "cinema", "game")) return "Entertainment";
        if (containsAny(text, "salary", "wage", "incasare")) return "Income";
        if (containsAny(text, "pharmacy", "doctor", "health")) return "Health";

        return "Utilities";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}