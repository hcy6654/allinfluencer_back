package com.allinfluencer.backend.auth.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserIdentityRepository extends JpaRepository<UserIdentityEntity, String> {
    Optional<UserIdentityEntity> findByProviderAndProviderUserId(String provider, String providerUserId);
    List<UserIdentityEntity> findByUserIdOrderByLinkedAtDesc(String userId);
    long countByUserId(String userId);
    long deleteByUserIdAndProvider(String userId, String provider);
}

