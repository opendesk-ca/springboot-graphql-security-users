package com.graphql.userservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/users/register").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String issuerUri = "http://localhost:8180/realms/graphql";
        log.info("Configuring JWT decoder for issuer: {}", issuerUri);
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // Extract realm roles
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                Collection<String> realmRoles = (Collection<String>) realmAccess.get("roles");
                realmRoles.forEach(role -> {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                    log.debug("Added realm role: ROLE_{}", role.toUpperCase());
                });
            }

            // Extract client roles
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                resourceAccess.forEach((clientId, clientAccess) -> {
                    if (clientAccess instanceof Map) {
                        Map<String, Object> clientAccessMap = (Map<String, Object>) clientAccess;
                        if (clientAccessMap.containsKey("roles")) {
                            Collection<String> clientRoles = (Collection<String>) clientAccessMap.get("roles");
                            clientRoles.forEach(role -> {
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                                log.debug("Added client role from {}: ROLE_{}", clientId, role.toUpperCase());
                            });
                        }
                    }
                });
            }

            log.debug("Total authorities extracted: {}", authorities.size());
            if (authorities.isEmpty()) {
                log.warn("No roles found in JWT token - this might cause authorization issues");
            }

            return authorities;
        });
        return converter;
    }
}