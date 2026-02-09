package com.financeassistant.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "microservice-ai", configuration = FeignClientInterceptor.class)
public interface AIFeignClient {

    @PostMapping("/api/ai/categorize")
    String predictCategory(@RequestBody String description);
}