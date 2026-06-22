package org.example.service;


import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class KeycloakAdminService {

    private final Keycloak keycloak;
    private final String realm;

    public KeycloakAdminService() {
        this.realm = System.getenv("KEYCLOAK_REALM");

        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(System.getenv("KEYCLOAK_SERVER_URL"))
                .realm(System.getenv("KEYCLOAK_REALM"))
                .username(System.getenv("KEYCLOAK_ADMIN"))
                .password(System.getenv("KEYCLOAK_ADMIN_PASSWORD"))
                .clientId("admin-cli")
                .build();
        //                .serverUrl("http://localhost:8080")
//                .realm("master")
//                .username("andreiShel")
//                .password("keycloakShel")
//                .clientId("admin-cli")
//                .build();
    }

    public void createUser(String username, String password, String role) {
        boolean userExists = !keycloak.realm(realm).users().search(username).isEmpty();
        if (userExists) {
            System.out.println("User '" + username + "' already exists, skipping creation.");
            return;
        }
        if (role == null || role.isEmpty()) {
            role = "USER";
        }


        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setRequiredActions(Collections.emptyList());


        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        user.setCredentials(Collections.singletonList(credential));


        Response response = keycloak.realm(realm).users().create(user);
        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user: HTTP " + response.getStatus());
        }

        String userId = response.getLocation().getPath().replaceAll(".*/(.*)$", "$1");


        keycloak.realm(realm)
                .users().get(userId)
                .roles().realmLevel()
                .add(Collections.singletonList(
                        keycloak.realm(realm)
                                .roles()
                                .get(role.toUpperCase())
                                .toRepresentation()
                ));
    }
}