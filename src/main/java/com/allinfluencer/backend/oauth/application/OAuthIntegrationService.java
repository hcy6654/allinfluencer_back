package com.allinfluencer.backend.oauth.application;

import com.allinfluencer.backend.auth.application.AuthApplicationService;
import com.allinfluencer.backend.auth.infrastructure.jpa.UserEntity;
import com.allinfluencer.backend.auth.infrastructure.jpa.UserIdentityEntity;
import com.allinfluencer.backend.auth.infrastructure.jpa.UserIdentityRepository;
import com.allinfluencer.backend.auth.infrastructure.jpa.UserRepository;
import com.allinfluencer.backend.mypage.domain.DomainException;
import com.allinfluencer.backend.signup.domain.VerificationStatus;
import com.allinfluencer.backend.signup.infrastructure.jpa.InfluencerVerificationEntity;
import com.allinfluencer.backend.signup.infrastructure.jpa.InfluencerVerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class OAuthIntegrationService {
    private final UserRepository userRepository;
    private final UserIdentityRepository identityRepository;
    private final InfluencerVerificationRepository influencerVerificationRepository;

    public OAuthIntegrationService(
            UserRepository userRepository,
            UserIdentityRepository identityRepository,
            InfluencerVerificationRepository influencerVerificationRepository
    ) {
        this.userRepository = userRepository;
        this.identityRepository = identityRepository;
        this.influencerVerificationRepository = influencerVerificationRepository;
    }

    @Transactional
    public IntegrationResult integrate(String provider, OAuthProfile profile, String linkingUserId) {
        String normalizedProvider = provider.toUpperCase();

        // 1) 이미 연결된 소셜 계정이면 바로 로그인 대상 사용자 결정
        var existingIdentity = identityRepository
                .findByProviderAndProviderUserId(normalizedProvider, profile.providerUserId());
        if (existingIdentity.isPresent()) {
            var user = userRepository.findById(existingIdentity.get().getUserId())
                    .orElseThrow(() -> new DomainException("NOT_FOUND", "User not found"));
            return new IntegrationResult(user, false, false, profile.email() == null);
        }

        // 2) linking 모드면 현재 로그인 사용자에 연결
        if (linkingUserId != null && !linkingUserId.isBlank()) {
            var user = userRepository.findById(linkingUserId)
                    .orElseThrow(() -> new DomainException("NOT_FOUND", "User not found"));

            identityRepository.save(new UserIdentityEntity(
                    UUID.randomUUID().toString(),
                    user.getId(),
                    normalizedProvider,
                    profile.providerUserId(),
                    profile.email()
            ));

            return new IntegrationResult(user, false, true, profile.email() == null);
        }

        // 3) email이 있으면 기존 계정에 연결(통합) or 새 계정 생성
        if (profile.email() != null && !profile.email().isBlank()) {
            var existingUser = userRepository.findByEmail(profile.email()).orElse(null);
            if (existingUser != null) {
                identityRepository.save(new UserIdentityEntity(
                        UUID.randomUUID().toString(),
                        existingUser.getId(),
                        normalizedProvider,
                        profile.providerUserId(),
                        profile.email()
                ));
                return new IntegrationResult(existingUser, false, true, false);
            }
        }

        // 4) 새 계정 생성 (기본: INFLUENCER / 승인 필요)
        String userId = UUID.randomUUID().toString();
        UserEntity user = new UserEntity(
                userId,
                profile.email(),
                null,
                profile.displayName(),
                "INFLUENCER",
                "INACTIVE"
        );
        userRepository.save(user);

        identityRepository.save(new UserIdentityEntity(
                UUID.randomUUID().toString(),
                userId,
                normalizedProvider,
                profile.providerUserId(),
                profile.email()
        ));

        // 승인 대기 생성
        influencerVerificationRepository.save(new InfluencerVerificationEntity(
                UUID.randomUUID().toString(),
                userId,
                VerificationStatus.PENDING,
                normalizedProvider,
                LocalDateTime.now(),
                null
        ));

        return new IntegrationResult(user, true, true, profile.email() == null);
    }

    public record OAuthProfile(
            String providerUserId,
            String email,
            String displayName,
            String avatar
    ) {}

    public record IntegrationResult(
            UserEntity user,
            boolean isNewUser,
            boolean isNewIdentity,
            boolean needsEmailVerification
    ) {}
}

