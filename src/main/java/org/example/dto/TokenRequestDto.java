package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequestDto {
    
    @NotBlank(message = "Grant type is required")
    @Pattern(regexp = "password|refresh_token", message = "Grant type must be either 'password' or 'refresh_token'")
    private String grant_type;
    
    private String username;
    
    private String password;
    
    private String refresh_token;
}