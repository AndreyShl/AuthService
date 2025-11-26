package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
    private Integer refreshExpiresIn;
}