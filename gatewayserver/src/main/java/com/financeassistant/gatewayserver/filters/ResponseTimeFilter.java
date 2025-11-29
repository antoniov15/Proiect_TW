package com.financeassistant.gatewayserver.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ResponseTimeFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ResponseTimeFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();

        exchange.getResponse().beforeCommit(() -> {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            logger.info("ADDING HEADER: X-Response-Time-Ms: {}", duration);

            exchange.getResponse().getHeaders().add("X-Response-Time-Ms", String.valueOf(duration));
            return Mono.empty();
        });

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Request {} processed in {} ms.", exchange.getRequest().getPath(), duration);
        }));
    }

    @Override
    public int getOrder() {
        return 10;
    }
}