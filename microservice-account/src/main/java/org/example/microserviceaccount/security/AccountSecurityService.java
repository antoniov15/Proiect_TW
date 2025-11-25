package org.example.microserviceaccount.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

public class AccountSecurityService {
    public boolean isOwnerOrAdmin(Authentication authentication, Long accountId) {
        if (authentication == null) {
            return false;
        }

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return true;
        }

        String username = authentication.getName();

        return username != null && username.equals(String.valueOf(accountId));
    }
}
