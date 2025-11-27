package com.financeassistant.gatewayserver.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(1)
@Component
public class JwtValidationFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtValidationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER);

        if (authHeader == null || authHeader.isEmpty()) {
            logger.debug("No Authorization header, skipping JWT validation for request: {}",
                    exchange.getRequest().getURI());
            return chain.filter(exchange);
        }

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            logger.warn("Authorization header present but not Bearer for request: {}",
                    exchange.getRequest().getURI());
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (!isValidToken(token)) {
            logger.warn("Invalid JWT token for request: {}", exchange.getRequest().getURI());
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        logger.debug("JWT token validated successfully for request: {}", exchange.getRequest().getURI());
        return chain.filter(exchange);
    }

    private boolean isValidToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        String[] parts = token.split("\\.");
        return parts.length == 3;
    }
}
