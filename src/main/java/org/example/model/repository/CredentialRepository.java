package org.example.model.repository;

import org.example.model.entity.CredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface CredentialRepository extends JpaRepository<CredentialEntity, UUID> {


    Optional<CredentialEntity> findByLogin(String login);

    boolean existsByLogin(String login);
}
