package com.financeassistant.gatewayserver.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/user")
    public Mono<Map<String, Object>> user(@AuthenticationPrincipal OAuth2User principal) {
        return Mono.just(principal.getAttributes());
    }

    @GetMapping("/")
    public Mono<String> home(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return Mono.just("Welcome! Please log in.");
        }
        return Mono.just("Welcome " + principal.getAttribute("name"));
    }
}
