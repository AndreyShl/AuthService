package org.example.service;

import org.example.dto.CredentialRequestDto;
import org.example.mapper.CredentialMapper;
import org.example.model.entity.CredentialEntity;
import org.example.model.repository.CredentialRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;

@Service
public class CredentialService {

    private final CredentialRepository repository;
    private final CredentialMapper credentialMapper;

    private final Keycloak keycloak;

    public CredentialService(CredentialRepository repository, CredentialMapper credentialMapper) {
        this.repository = repository;
        this.credentialMapper = credentialMapper;
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl("http://localhost:8080")
                .realm("master")
                .username("andreiShel")
                .password("keycloakShel")
                .clientId("admin-cli")
                .build();
    }

    public CredentialEntity createCredential(UUID userId, String login, String password, String role) {
        if (repository.existsByLogin(login)) {
            throw new RuntimeException("Login already exists");
        }

        CredentialRequestDto dto = new CredentialRequestDto(login, password, role);
        return createCredential(dto, userId);
    }

    public CredentialEntity createCredential(CredentialRequestDto dto, UUID userId) {
        if (repository.existsByLogin(dto.getLogin())) {
            throw new RuntimeException("Login already exists");
        }

        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(dto.getPassword(), salt);

        CredentialEntity entity = credentialMapper.toEntity(dto, userId);
        entity.setPasswordHash(hashedPassword);

        createUserInKeycloak(dto);

        return repository.save(entity);
    }

    public Optional<CredentialEntity> findByLogin(String login) {
        return repository.findByLogin(login);
    }

    public boolean checkPassword(CredentialEntity credential, String password) {
        return BCrypt.checkpw(password, credential.getPasswordHash());
    }
    private void createUserInKeycloak(CredentialRequestDto dto) {

        UserRepresentation user = new UserRepresentation();
        user.setUsername(dto.getLogin());
        user.setEnabled(true);


        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(dto.getPassword());
        credential.setTemporary(false);
        user.setCredentials(java.util.List.of(credential));


        keycloak.realm("auth-realm").users().create(user);


    }
}
