package com.allinfluencer.backend.signup.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertiserVerificationRepository extends JpaRepository<AdvertiserVerificationEntity, String> {
    java.util.Optional<AdvertiserVerificationEntity> findFirstByUserIdOrderBySubmittedAtDesc(String userId);
}

