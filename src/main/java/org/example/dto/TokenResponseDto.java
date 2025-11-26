package org.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDto {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("expires_in")
    private String expiresIn;
    
    @JsonProperty("refresh_expires_in")
    private String refreshExpiresIn;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("not-before-policy")
    private Integer notBeforePolicy;
    
    @JsonProperty("session_state")
    private String sessionState;
    
    @JsonProperty("scope")
    private String scope;
}