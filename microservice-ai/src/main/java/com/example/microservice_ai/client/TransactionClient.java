package com.example.microservice_ai.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.microservice_ai.dto.external.CreateTransactionRequestDTO;
import com.example.microservice_ai.dto.external.TransactionResponseDTO;

@FeignClient(name = "microservice-transactions", path = "/api/transactions")
public interface TransactionClient {

    @PostMapping
    ResponseEntity<TransactionResponseDTO> createTransaction(@RequestBody CreateTransactionRequestDTO dto);

    @GetMapping("/user/{userId}")
    ResponseEntity<List<TransactionResponseDTO>> getTransactionsByUserId(@PathVariable("userId") Long userId);
}
