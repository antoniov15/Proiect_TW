package com.example.microservice_ai.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "microservice-account", path = "/api/v1/accounts")
public interface AccountClient {

    @GetMapping("/lookup")
    ResponseEntity<Long> getUserIdByEmail(@RequestParam("email") String email);
}
