package com.financeassistant.gatewayserver.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.lang.NonNull; // Import pentru warning
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GoogleIdTokenRelayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth.getPrincipal() instanceof OidcUser)
                .map(auth -> (OidcUser) auth.getPrincipal())
                .map(oidcUser -> {
                    // Cazul 1: User Logat -> Creăm un Exchange modificat (mutated)
                    String idToken = oidcUser.getIdToken().getTokenValue();
                    return withBearerToken(exchange, idToken);
                })
                // Cazul 2: User Nelogat -> Folosim Exchange-ul original
                .defaultIfEmpty(exchange)
                // Pasul Final: Executăm lanțul O SINGURĂ DATĂ cu exchange-ul ales
                .flatMap(chain::filter);
    }

    private ServerWebExchange withBearerToken(ServerWebExchange exchange, String idToken) {
        ServerHttpRequestDecorator decoratedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            @NonNull // Rezolvă warning-ul din IntelliJ
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.putAll(super.getHeaders());
                headers.set("Authorization", "Bearer " + idToken);
                return headers;
            }
        };
        return exchange.mutate().request(decoratedRequest).build();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}