package org.example.microserviceaccount.client;

import org.example.microserviceaccount.dto.external.TransactionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "microservice-transactions")
public interface TransactionClient {

    // mapare endpoint din TransactionController: @GetMapping("/user/{userId}")
    @GetMapping("/api/transactions/user/{userId}")
    List<TransactionDTO> getTransactionsByUserId(@PathVariable("userId") Long userId);
}
