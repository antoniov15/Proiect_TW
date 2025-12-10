package com.financeassistant.gatewayserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class GatewayController {

    @GetMapping
    public ResponseEntity<String> index(){
        return ResponseEntity.ok("Hello to Finance Assistant Portal");
    }

    //Google access token
    @GetMapping("/access-token")
    public String getAccessToken(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client) {
        OAuth2AccessToken token = client.getAccessToken();
        return token.getTokenValue();
    }

    /*
    OidcUser = interface for auth OpenID Connect user. Extends OAuth2User
     */
    @GetMapping("/id-token")
    public Map<String, Object> getIdToken(@AuthenticationPrincipal OidcUser oidcUser) {
        // OidcUser is only available if you requested 'openid' scope
        Map<String, Object> info = new HashMap<>();
        info.put("idToken", oidcUser.getIdToken().getTokenValue());
        info.put("claims", oidcUser.getClaims());
        info.put("authorities", oidcUser.getAuthorities());
        return info;
    }
}
