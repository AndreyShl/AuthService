package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;


@Data
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