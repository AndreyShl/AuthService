package org.example.mapper;

import org.example.dto.CredentialRequestDto;
import org.example.dto.CredentialResponseDto;
import org.example.model.entity.CredentialEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;


@Mapper
public interface CredentialMapper {

    CredentialResponseDto toDto(CredentialEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    CredentialEntity toEntity(CredentialRequestDto dto);
    

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    CredentialEntity toEntity(CredentialRequestDto dto, UUID userId);
}