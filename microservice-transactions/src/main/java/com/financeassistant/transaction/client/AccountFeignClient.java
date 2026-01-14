package com.financeassistant.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "microservice-account", configuration = FeignClientInterceptor.class)
public interface AccountFeignClient {

    @GetMapping("/api/v1/accounts/lookup")
    Long getUserIdByEmail(@RequestParam("email") String email);
}