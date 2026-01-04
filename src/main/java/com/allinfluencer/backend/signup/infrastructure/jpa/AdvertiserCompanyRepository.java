package com.allinfluencer.backend.signup.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdvertiserCompanyRepository extends JpaRepository<AdvertiserCompanyEntity, String> {
    Optional<AdvertiserCompanyEntity> findByUserId(String userId);
}

