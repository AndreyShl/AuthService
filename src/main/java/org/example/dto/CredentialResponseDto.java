package org.example.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CredentialResponseDto {
    
    private UUID id;
    private UUID userId;
    private String login;
    private String role;
    private Boolean active;
    private OffsetDateTime createdAt;
}