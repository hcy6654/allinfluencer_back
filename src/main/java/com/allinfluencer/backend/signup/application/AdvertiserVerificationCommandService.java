package com.allinfluencer.backend.signup.application;

import com.allinfluencer.backend.mypage.domain.DomainException;
import com.allinfluencer.backend.signup.infrastructure.jpa.AdvertiserVerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdvertiserVerificationCommandService {
    private final AdvertiserVerificationRepository advertiserVerificationRepository;

    public AdvertiserVerificationCommandService(AdvertiserVerificationRepository advertiserVerificationRepository) {
        this.advertiserVerificationRepository = advertiserVerificationRepository;
    }

    @Transactional
    public void verify(String advertiserUserId) {
        var verification = advertiserVerificationRepository
                .findFirstByUserIdOrderBySubmittedAtDesc(advertiserUserId)
                .orElseThrow(() -> new DomainException("NOT_FOUND", "인증 요청을 찾을 수 없습니다."));
        verification.markVerified(LocalDateTime.now());
    }

    @Transactional
    public void reject(String advertiserUserId) {
        var verification = advertiserVerificationRepository
                .findFirstByUserIdOrderBySubmittedAtDesc(advertiserUserId)
                .orElseThrow(() -> new DomainException("NOT_FOUND", "인증 요청을 찾을 수 없습니다."));
        verification.markRejected(LocalDateTime.now());
    }
}

