package com.allinfluencer.backend.signup.application;

import com.allinfluencer.backend.auth.infrastructure.jpa.UserRepository;
import com.allinfluencer.backend.mypage.domain.DomainException;
import com.allinfluencer.backend.signup.infrastructure.jpa.InfluencerVerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InfluencerApprovalCommandService {
    private final UserRepository userRepository;
    private final InfluencerVerificationRepository influencerVerificationRepository;

    public InfluencerApprovalCommandService(
            UserRepository userRepository,
            InfluencerVerificationRepository influencerVerificationRepository
    ) {
        this.userRepository = userRepository;
        this.influencerVerificationRepository = influencerVerificationRepository;
    }

    @Transactional
    public void approve(String influencerUserId) {
        var user = userRepository.findById(influencerUserId)
                .orElseThrow(() -> new DomainException("NOT_FOUND", "사용자를 찾을 수 없습니다."));

        if (!"INFLUENCER".equalsIgnoreCase(user.getRole())) {
            throw new DomainException("INVALID_ARGUMENT", "인플루언서 사용자만 승인할 수 있습니다.");
        }

        var verification = influencerVerificationRepository
                .findFirstByUserIdOrderBySubmittedAtDesc(influencerUserId)
                .orElseThrow(() -> new DomainException("NOT_FOUND", "인증 요청을 찾을 수 없습니다."));

        verification.markVerified(LocalDateTime.now());
        user.setStatus("ACTIVE");
        // save는 트랜잭션 dirty-checking으로 반영됨
    }

    @Transactional
    public void reject(String influencerUserId) {
        var user = userRepository.findById(influencerUserId)
                .orElseThrow(() -> new DomainException("NOT_FOUND", "사용자를 찾을 수 없습니다."));

        if (!"INFLUENCER".equalsIgnoreCase(user.getRole())) {
            throw new DomainException("INVALID_ARGUMENT", "인플루언서 사용자만 반려할 수 있습니다.");
        }

        var verification = influencerVerificationRepository
                .findFirstByUserIdOrderBySubmittedAtDesc(influencerUserId)
                .orElseThrow(() -> new DomainException("NOT_FOUND", "인증 요청을 찾을 수 없습니다."));

        verification.markRejected(LocalDateTime.now());
        user.setStatus("INACTIVE");
    }
}

