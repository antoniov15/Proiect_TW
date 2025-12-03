package org.example.microserviceaccount.security;

import org.example.microserviceaccount.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service("accountSecurity")
public class AccountSecurityService {
    @Autowired
    private AccountRepository accountRepository;


    public boolean isOwnerOrAdmin(Authentication authentication, Long accountId) {
        if (authentication == null) {
            return false;
        }

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return true;
        }

        String userEmail = null;
        if(authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            userEmail = jwt.getClaimAsString("email");
        }

        if(userEmail == null) {
            return false;
        }

        String finalUserEmail = userEmail;
        return accountRepository.findById(accountId)
                .map(account -> account.getEmail().equals(finalUserEmail))
                .orElse(false);
    }
}
