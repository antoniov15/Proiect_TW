package com.financeassistant.gatewayserver.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Component
public class TraceIdFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(TraceIdFilter.class);
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders originalHeaders = exchange.getRequest().getHeaders();
        String traceId = getTraceId(originalHeaders);

        logger.info("Primire cerere cu Trace ID: {}", traceId);

        HttpHeaders mutableHeaders = new HttpHeaders();
        mutableHeaders.putAll(originalHeaders);
        mutableHeaders.set(TRACE_ID_HEADER, traceId);

        ServerHttpRequest decoratedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                return mutableHeaders;
            }
        };

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(decoratedRequest)
                .build();

        mutatedExchange.getResponse().getHeaders().set(TRACE_ID_HEADER, traceId);

        return chain.filter(mutatedExchange);
    }

    private String getTraceId(HttpHeaders headers) {
        if (headers.containsKey(TRACE_ID_HEADER)) {
            return headers.getFirst(TRACE_ID_HEADER);
        }
        return UUID.randomUUID().toString();
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
