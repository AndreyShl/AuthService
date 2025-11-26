package org.example.mapper;

import org.example.dto.LoginResponseDto;
import org.example.dto.RefreshTokenResponseDto;
import org.example.dto.TokenResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;


@Mapper
public interface TokenMapper {
    

    @Mapping(target = "accessToken", source = "access_token")
    @Mapping(target = "refreshToken", source = "refresh_token")
    @Mapping(target = "expiresIn", source = "expires_in")
    @Mapping(target = "refreshExpiresIn", source = "refresh_expires_in")
    @Mapping(target = "tokenType", source = "token_type")
    @Mapping(target = "notBeforePolicy", source = "not-before-policy")
    @Mapping(target = "sessionState", source = "session_state")
    TokenResponseDto toTokenResponseDto(Map<String, String> tokenMap);
    

    @Mapping(target = "accessToken", source = "access_token")
    @Mapping(target = "refreshToken", source = "refresh_token")
    @Mapping(target = "expiresIn", source = "expires_in")
    @Mapping(target = "refreshExpiresIn", source = "refresh_expires_in")
    LoginResponseDto toLoginResponseDto(Map<String, String> tokenMap);
    

    @Mapping(target = "accessToken", source = "access_token")
    @Mapping(target = "refreshToken", source = "refresh_token")
    @Mapping(target = "expiresIn", source = "expires_in")
    @Mapping(target = "refreshExpiresIn", source = "refresh_expires_in")
    RefreshTokenResponseDto toRefreshTokenResponseDto(Map<String, String> tokenMap);
    

    LoginResponseDto tokenToLoginResponse(TokenResponseDto tokenResponseDto);

    RefreshTokenResponseDto tokenToRefreshResponse(TokenResponseDto tokenResponseDto);
}