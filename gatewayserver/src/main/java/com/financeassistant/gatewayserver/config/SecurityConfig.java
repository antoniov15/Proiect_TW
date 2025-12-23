package com.financeassistant.gatewayserver.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.Binding;
import com.google.api.services.cloudresourcemanager.model.GetIamPolicyRequest;
import com.google.api.services.cloudresourcemanager.model.Policy;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
//@Profile("test")
@Profile("!dev")
public class SecurityConfig {

    //TODO: set your GCP project ID
    private final String idProject = "finance-assistant-gateway";

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/", "/index.html", "/login/**", "/oauth2/**", "/actuator/**").permitAll()

                        .pathMatchers(HttpMethod.DELETE, "/microservice-transactions/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler(successHandler())
                )
                .oauth2Client(Customizer.withDefaults())
                .build();
    }

    @Bean
    public ServerAuthenticationSuccessHandler successHandler() {
        return (webFilterExchange, authentication) -> {
            System.out.println("Authenticated authorities at success: " + authentication.getAuthorities());

            webFilterExchange
                    .getExchange()
                    .getResponse()
                    .setStatusCode(org.springframework.http.HttpStatus.FOUND);

            webFilterExchange
                    .getExchange()
                    .getResponse()
                    .getHeaders().set("Location", "http://localhost:8072/");

            return webFilterExchange.getExchange().getResponse().setComplete();
        };
    }

    /*
    OIDC (OpenID Connect)
    Allows the client to know who the user is and get user information.
    Uses ID Tokens (JWTs) to carry user identity information
     */
    @Bean
    public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcReactiveOAuth2UserService delegate = new OidcReactiveOAuth2UserService();

        return new ReactiveOAuth2UserService<OidcUserRequest, OidcUser>() {
            @Override
            public Mono<OidcUser> loadUser(OidcUserRequest userRequest) {
                return delegate.loadUser(userRequest)
                        .map(oidcUser -> {
                            Set<GrantedAuthority> mappedAuthorities = new HashSet<>(oidcUser.getAuthorities());

                            try {
                                Set<GrantedAuthority> iamRoles = getIamRoles(userRequest, oidcUser);
                                mappedAuthorities.addAll(iamRoles);

                            }catch (GeneralSecurityException | IOException e) {
                                System.out.println(e.getMessage());
                            }

                            System.out.println("Mapped authorities: " + mappedAuthorities);

                            return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
                        });
            }
        };
    }

    private Set<GrantedAuthority> getIamRoles(OidcUserRequest userRequest, OidcUser oidcUser) throws GeneralSecurityException, IOException {

        String accessTokenValue = userRequest.getAccessToken().getTokenValue();
        System.out.println("accessTokenValue: " + accessTokenValue);

        AccessToken accessToken = new AccessToken(accessTokenValue, Date.from(userRequest.getAccessToken().getExpiresAt()));
        System.out.println("accessToken: " + accessToken.getTokenValue());

        GoogleCredentials credentials = GoogleCredentials.create(accessToken);
        System.out.println("credentials: " + credentials);

        CloudResourceManager handler = new CloudResourceManager.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("GatewayServer")
                .build();

        GetIamPolicyRequest policyRequest = new GetIamPolicyRequest();
        Policy policy =  handler.projects().getIamPolicy(idProject, policyRequest).execute();
        System.out.println("policy: " + policy);

        String email = oidcUser.getEmail();
        String identifier = "user:" + email;

        return policy.getBindings().stream()
                .filter(binding -> binding.getMembers() != null && binding.getMembers().contains(identifier))
                .map(Binding::getRole)
                .peek(role -> System.out.println("Role is: " + role))
                .map(this::mapIamRolesToApplicationRoles)
                .collect(Collectors.toSet());

    }

    private GrantedAuthority mapIamRolesToApplicationRoles(String role) {

        if ("roles/owner".equals(role)) {
            return new SimpleGrantedAuthority("ROLE_ADMIN");
        }
        if ("roles/editor".equals(role)) {
            return new SimpleGrantedAuthority("ROLE_USER");
        }
        if ("roles/viewer".equals(role)) {
            return new SimpleGrantedAuthority("ROLE_USER");
        }
        return new SimpleGrantedAuthority("ROLE_GUEST");
    }


}
