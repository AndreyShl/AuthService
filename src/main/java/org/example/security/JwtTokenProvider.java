package org.example.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity; // in seconds

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity; // in seconds

    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthServerUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${keycloak.resource}")
    private String keycloakClientId;

    @Value("${keycloak.credentials.secret}")
    private String keycloakClientSecret;

    private final RestTemplate restTemplate;

    public JwtTokenProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private Key getSigningKey() {
        return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS512.getJcaName());
    }


    public Map<String, String> getKeycloakTokens(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", keycloakClientId);
        map.add("client_secret", keycloakClientSecret);
        map.add("grant_type", "password");
        map.add("username", username);
        map.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                keycloakAuthServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, String>>() {}
        );

        return response.getBody();
    }


    public Map<String, String> refreshKeycloakToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", keycloakClientId);
        map.add("client_secret", keycloakClientSecret);
        map.add("grant_type", "refresh_token");
        map.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                keycloakAuthServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token",
                HttpMethod.POST,
                request,
                Map.class
        );

        Map<String, String> tokens = response.getBody();
        return tokens;
    }


    public boolean validateKeycloakToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", keycloakClientId);
        map.add("client_secret", keycloakClientSecret);
        map.add("token", token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    keycloakAuthServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token/introspect",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> result = response.getBody();
            return result != null && result.containsKey("active") ? (Boolean) result.get("active") : false;
        } catch (Exception e) {
            return false;
        }
    }


    public String generateAccessToken(UUID userId, String role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenValidity * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(UUID userId, String role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenValidity * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public UUID getUserIdFromToken(String token) {

        return UUID.fromString(getClaims(token).getSubject());
    }

    public String getRoleFromToken(String token) {

        return getClaims(token).get("role", String.class);
    }
}
