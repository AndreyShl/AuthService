package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.*;
import org.example.exception.TokenExpiredException;
import org.example.exception.UnauthorizedException;
import org.example.mapper.CredentialMapper;
import org.example.mapper.TokenMapper;
import org.example.model.entity.CredentialEntity;
import org.example.service.CredentialService;
import org.example.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final CredentialService credentialService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CredentialMapper credentialMapper;
    private final TokenMapper tokenMapper;

    public AuthController(CredentialService credentialService, JwtTokenProvider jwtTokenProvider,
                          CredentialMapper credentialMapper, TokenMapper tokenMapper) {
        this.credentialService = credentialService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.credentialMapper = credentialMapper;
        this.tokenMapper = tokenMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        if (loginRequest.getLogin() == null || loginRequest.getPassword() == null) {
            throw new BadCredentialsException("Username and password are required");
        }

        try {

            Map<String, String> tokens = jwtTokenProvider.getKeycloakTokens(loginRequest.getLogin(), loginRequest.getPassword());
            LoginResponseDto response = tokenMapper.toLoginResponseDto(tokens);
            return ResponseEntity.ok(response);
        } catch (HttpClientErrorException e) {

            if (e.getStatusCode().value() == 401) {
                throw new BadCredentialsException("Invalid username or password");
            }
            throw e;
        } catch (Exception e) {
            try {
                CredentialEntity credential = credentialService.findByLogin(loginRequest.getLogin())
                        .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

                if (!credentialService.checkPassword(credential, loginRequest.getPassword())) {
                    throw new BadCredentialsException("Invalid username or password");
                }

                String accessToken = jwtTokenProvider.generateAccessToken(credential.getUserId(), credential.getRole());
                String refreshToken = jwtTokenProvider.generateRefreshToken(credential.getUserId(), credential.getRole());

                LoginResponseDto response = LoginResponseDto.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();

                return ResponseEntity.ok(response);
            } catch (BadCredentialsException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new UnauthorizedException("Authentication failed", ex);
            }
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto refreshRequest) {
        if (refreshRequest.getRefreshToken() == null) {
            throw new BadCredentialsException("Refresh token is required");
        }

        try {
            Map<String, String> tokens = jwtTokenProvider.refreshKeycloakToken(refreshRequest.getRefreshToken());
            RefreshTokenResponseDto response = tokenMapper.toRefreshTokenResponseDto(tokens);
            return ResponseEntity.ok(response);

        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            if (e.getStatusCode().value() == 400 || e.getStatusCode().value() == 401) {
                throw new TokenExpiredException("Invalid or expired refresh token");
            }
            throw e;
        } catch (Exception e) {
            try {
                if (!jwtTokenProvider.validateToken(refreshRequest.getRefreshToken())) {
                    throw new TokenExpiredException("Invalid or expired refresh token");
                }

                UUID userId = jwtTokenProvider.getUserIdFromToken(refreshRequest.getRefreshToken());
                String role = jwtTokenProvider.getRoleFromToken(refreshRequest.getRefreshToken());
                String newAccessToken = jwtTokenProvider.generateAccessToken(userId, role);

                RefreshTokenResponseDto response = RefreshTokenResponseDto.builder()
                        .accessToken(newAccessToken)
                        .build();

                return ResponseEntity.ok(response);
            } catch (TokenExpiredException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new UnauthorizedException("Token refresh failed", ex);
            }
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidationResponseDto> validate(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid authorization header");
        }

        String token = authHeader.replace("Bearer ", "");

        if (token.isEmpty()) {
            throw new UnauthorizedException("Token is required");
        }

        try {
            boolean valid = jwtTokenProvider.validateKeycloakToken(token);
            if (!valid) {
                throw new TokenExpiredException("Token is invalid or expired");
            }
            return ResponseEntity.ok(ValidationResponseDto.builder()
                    .valid(true)
                    .message("Token is valid")
                    .build());
        } catch (HttpClientErrorException e) {

            throw new UnauthorizedException("Token validation failed: " + e.getMessage());
        } catch (TokenExpiredException e) {
            throw e;
        } catch (Exception e) {
            try {
                boolean valid = jwtTokenProvider.validateToken(token);
                if (!valid) {
                    throw new TokenExpiredException("Token is invalid or expired");
                }
                return ResponseEntity.ok(ValidationResponseDto.builder()
                        .valid(true)
                        .message("Token is valid")
                        .build());
            } catch (TokenExpiredException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new UnauthorizedException("Token validation failed", ex);
            }
        }
    }

    @PostMapping("/token")
    public ResponseEntity<TokenResponseDto> getToken(@Valid @RequestBody TokenRequestDto tokenRequest) {
        String grantType = tokenRequest.getGrantType();

        if (grantType == null) {
            throw new BadCredentialsException("Grant type is required");
        }

        if ("password".equals(grantType)) {
            String username = tokenRequest.getUsername();
            String password = tokenRequest.getPassword();

            if (username == null || password == null) {
                throw new BadCredentialsException("Username and password are required");
            }

            try {
                Map<String, String> tokens = jwtTokenProvider.getKeycloakTokens(username, password);
                TokenResponseDto response = tokenMapper.toTokenResponseDto(tokens);
                return ResponseEntity.ok(response);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 401) {
                    throw new BadCredentialsException("Invalid username or password");
                }
                throw e;
            } catch (Exception e) {
                throw new UnauthorizedException("Authentication failed", e);
            }
        } else if ("refresh_token".equals(grantType)) {
            String refreshToken = tokenRequest.getRefreshToken();

            if (refreshToken == null) {
                throw new BadCredentialsException("Refresh token is required");
            }

            try {
                Map<String, String> tokens = jwtTokenProvider.refreshKeycloakToken(refreshToken);
                TokenResponseDto response = tokenMapper.toTokenResponseDto(tokens);
                return ResponseEntity.ok(response);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 400 || e.getStatusCode().value() == 401) {
                    throw new TokenExpiredException("Invalid or expired refresh token");
                }
                throw e;
            } catch (Exception e) {
                throw new UnauthorizedException("Token refresh failed", e);
            }
        } else {
            throw new BadCredentialsException("Unsupported grant type: " + grantType);
        }
    }

    @PostMapping("/credentials")
    public ResponseEntity<CredentialResponseDto> createUser(@Valid @RequestBody CredentialRequestDto credentialRequest) {
        if (credentialRequest.getLogin() == null || credentialRequest.getPassword() == null || credentialRequest.getRole() == null) {
            throw new BadCredentialsException("Login, password, and role are required");
        }

        if (!credentialRequest.getRole().equals("ADMIN") && !credentialRequest.getRole().equals("USER")) {
            throw new BadCredentialsException("Role must be either ADMIN or USER");
        }

        try {
            UUID userId = UUID.randomUUID();
            CredentialEntity credential = credentialService.createCredential(credentialRequest, userId);

            CredentialResponseDto response = credentialMapper.toDto(credential);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Login already exists")) {
                throw new BadCredentialsException("Login already exists");
            }
            throw e;
        }
    }
}
