package com.financeassistant.transaction.client;

import com.financeassistant.transaction.dto.AccountResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "microservice-account", configuration = FeignClientInterceptor.class)
public interface AccountFeignClient {

    @GetMapping("/api/v1/accounts/lookup")
    Long getUserIdByEmail(@RequestParam("email") String email);

    @GetMapping("/api/v1/accounts/{id}")
    AccountResponseDTO getAccountById(@PathVariable("id") Long id);
}