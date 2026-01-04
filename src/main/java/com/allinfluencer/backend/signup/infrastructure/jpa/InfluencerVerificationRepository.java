package com.allinfluencer.backend.signup.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InfluencerVerificationRepository extends JpaRepository<InfluencerVerificationEntity, String> {
    Optional<InfluencerVerificationEntity> findFirstByUserIdOrderBySubmittedAtDesc(String userId);
}

