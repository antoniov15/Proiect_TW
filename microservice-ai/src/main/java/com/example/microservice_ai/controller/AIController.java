package com.example.microservice_ai.controller;

import com.example.microservice_ai.service.CategorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final CategorizationService categorizationService;

    public AIController(CategorizationService categorizationService) {
        this.categorizationService = categorizationService;
    }

    @PostMapping("/categorize")
    public ResponseEntity<String> categorizeTransaction(@RequestBody String description) {
        String category = categorizationService.categorize(description);
        return ResponseEntity.ok(category);
    }
}