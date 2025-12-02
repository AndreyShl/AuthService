package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDto {
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}