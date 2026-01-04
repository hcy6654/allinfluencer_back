package com.allinfluencer.backend.auth.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshSessionRepository extends JpaRepository<RefreshSessionEntity, String> {
    Optional<RefreshSessionEntity> findByJti(String jti);
    long deleteByUserId(String userId);
    long countByUserId(String userId);
}

