package org.example.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
    }

    private String getPrincipalClaimName(Jwt jwt) {
        String claimName = "preferred_username";
        return jwt.getClaim(claimName);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        Collection<String> realmRoles = realmAccess != null ? (Collection<String>) realmAccess.get("roles") : Collections.emptyList();
        
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        Collection<String> resourceRoles = new ArrayList<>();
        
        if (resourceAccess != null) {

            String clientId = "auth-service";
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
            
            if (clientAccess != null && clientAccess.containsKey("roles")) {
                resourceRoles = (Collection<String>) clientAccess.get("roles");
            }
        }
        

        Stream<String> roles = Stream.concat(
                realmRoles.stream().map(role -> "ROLE_" + role.toUpperCase()),
                resourceRoles.stream().map(role -> "ROLE_" + role.toUpperCase())
        );
        

        Collection<GrantedAuthority> authorities = roles
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        

        authorities.addAll(defaultGrantedAuthoritiesConverter.convert(jwt));
        
        return authorities;
    }
}