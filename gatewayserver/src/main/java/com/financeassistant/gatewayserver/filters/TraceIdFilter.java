package com.financeassistant.gatewayserver.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
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
        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
        String traceId = getTraceId(requestHeaders);

        logger.info("Primire cerere cu Trace ID: {}", traceId);

        ServerWebExchange exchangeWithTraceId = exchange.mutate()
                .request(r -> r.header(TRACE_ID_HEADER, traceId))
                .build();

        exchangeWithTraceId.getResponse().getHeaders().add(TRACE_ID_HEADER, traceId);

        return chain.filter(exchangeWithTraceId);
    }

    private String getTraceId(HttpHeaders headers) {
        if(headers.containsKey(TRACE_ID_HEADER)){
            return headers.getFirst(TRACE_ID_HEADER);
        }
        return UUID.randomUUID().toString();
    }

    @Override
    public int getOrder() {
        return 1;
    }
}