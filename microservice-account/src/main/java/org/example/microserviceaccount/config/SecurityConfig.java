package org.example.microserviceaccount.config;

import org.example.microserviceaccount.entity.Account;
import org.example.microserviceaccount.repository.AccountRepository;

import org.example.microserviceaccount.entity.Account;
import org.example.microserviceaccount.repository.AccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
//    private static final List<String> ADMIN_EMAILS = List.of(
//            "laurentiu@student.unitbv.ro",
//            "antonio@student.unitbv.ro",
//            "dan.ignat@student.unitbv.ro",
//            "lau572004@gmail.com",
//            "antoniox2004.av@gmail.com"
//    );

    private final AccountRepository accountRepository;
    // inject repositry
    public SecurityConfig(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/v1/accounts/login", "/api/v1/accounts/reset-password").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String email = jwt.getClaimAsString("email");

            if (email != null) {
                Optional<Account> accountOpt = accountRepository.findByEmail(email);

                if (accountOpt.isPresent()) {
                    Account account = accountOpt.get();
                    return List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().toUpperCase()));
                }
            }

            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        });

        return converter;
    }
}
