package com.allinfluencer.backend.auth.application;

import com.allinfluencer.backend.auth.infrastructure.jpa.RefreshSessionEntity;
import com.allinfluencer.backend.auth.infrastructure.jpa.RefreshSessionRepository;
import com.allinfluencer.backend.auth.infrastructure.jpa.UserEntity;
import com.allinfluencer.backend.auth.infrastructure.jpa.UserRepository;
import com.allinfluencer.backend.mypage.domain.DomainException;
import com.allinfluencer.backend.signup.domain.BusinessRegistrationNumber;
import com.allinfluencer.backend.signup.domain.VerificationStatus;
import com.allinfluencer.backend.signup.infrastructure.jpa.AdvertiserCompanyEntity;
import com.allinfluencer.backend.signup.infrastructure.jpa.AdvertiserCompanyRepository;
import com.allinfluencer.backend.signup.infrastructure.jpa.AdvertiserVerificationEntity;
import com.allinfluencer.backend.signup.infrastructure.jpa.AdvertiserVerificationRepository;
import com.allinfluencer.backend.signup.infrastructure.jpa.InfluencerVerificationEntity;
import com.allinfluencer.backend.signup.infrastructure.jpa.InfluencerVerificationRepository;
import com.allinfluencer.backend.signup.infrastructure.mybatis.InfluencerProfileCommandMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static com.allinfluencer.backend.auth.application.JwtCookieTokenService.TokenPair;

@Service
public class AuthApplicationService {
    private final UserRepository userRepository;
    private final RefreshSessionRepository refreshSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtCookieTokenService tokenService;
    private final com.allinfluencer.backend.common.security.JwtProperties jwtProperties;
    private final AdvertiserCompanyRepository advertiserCompanyRepository;
    private final InfluencerVerificationRepository influencerVerificationRepository;
    private final AdvertiserVerificationRepository advertiserVerificationRepository;
    private final InfluencerProfileCommandMapper influencerProfileCommandMapper;

    public AuthApplicationService(
            UserRepository userRepository,
            RefreshSessionRepository refreshSessionRepository,
            PasswordEncoder passwordEncoder,
            JwtCookieTokenService tokenService,
            com.allinfluencer.backend.common.security.JwtProperties jwtProperties,
            AdvertiserCompanyRepository advertiserCompanyRepository,
            InfluencerVerificationRepository influencerVerificationRepository,
            AdvertiserVerificationRepository advertiserVerificationRepository,
            InfluencerProfileCommandMapper influencerProfileCommandMapper
    ) {
        this.userRepository = userRepository;
        this.refreshSessionRepository = refreshSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.jwtProperties = jwtProperties;
        this.advertiserCompanyRepository = advertiserCompanyRepository;
        this.influencerVerificationRepository = influencerVerificationRepository;
        this.advertiserVerificationRepository = advertiserVerificationRepository;
        this.influencerProfileCommandMapper = influencerProfileCommandMapper;
    }

    @Transactional
    public AuthResult signup(String email, String password, String displayName, String role, Object rolePayload) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new DomainException("INVALID_ARGUMENT", "이메일/비밀번호는 필수입니다.");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new DomainException("INVALID_ARGUMENT", "이미 가입된 이메일입니다.");
        }

        String userId = UUID.randomUUID().toString();
        String hash = passwordEncoder.encode(password);

        String normalizedRole = (role == null || role.isBlank()) ? "INFLUENCER" : role.trim().toUpperCase();
        // 인플루언서는 ADMIN 승인 전까지 로그인 불가 => status=INACTIVE로 생성
        String initialStatus = "INFLUENCER".equals(normalizedRole) ? "INACTIVE" : "ACTIVE";
        UserEntity user = new UserEntity(userId, email, hash, displayName, normalizedRole, initialStatus);
        userRepository.save(user);

        // 역할별 추가 입력 + 인증 요청 생성
        if ("ADVERTISER".equals(normalizedRole)) {
            // rolePayload는 컨트롤러에서 AdvertiserSignup으로 내려줌
            var p = (com.allinfluencer.backend.auth.presentation.dto.AuthDtos.AdvertiserSignup) rolePayload;
            if (p == null) throw new DomainException("INVALID_ARGUMENT", "사업자 회원가입 정보가 필요합니다.");

            var brn = new BusinessRegistrationNumber(p.businessRegistrationNumber());
            advertiserCompanyRepository.save(new AdvertiserCompanyEntity(
                    UUID.randomUUID().toString(),
                    userId,
                    p.companyName(),
                    p.industry(),
                    brn.value()
            ));

            advertiserVerificationRepository.save(new AdvertiserVerificationEntity(
                    UUID.randomUUID().toString(),
                    userId,
                    VerificationStatus.PENDING,
                    p.verification() == null ? null : p.verification().method(),
                    LocalDateTime.now(),
                    p.verification() == null ? null : p.verification().data()
            ));
        } else if ("INFLUENCER".equals(normalizedRole)) {
            var p = (com.allinfluencer.backend.auth.presentation.dto.AuthDtos.InfluencerSignup) rolePayload;
            if (p == null) throw new DomainException("INVALID_ARGUMENT", "인플루언서 회원가입 정보가 필요합니다.");
            if (p.categories() == null || p.categories().length == 0) {
                throw new DomainException("INVALID_ARGUMENT", "전문분야(categories)는 1개 이상 필요합니다.");
            }

            // influencer_profiles + channels 저장 (Command = MyBatis)
            String profileId = UUID.randomUUID().toString();
            LocalDateTime now = LocalDateTime.now();
            influencerProfileCommandMapper.insertInfluencerProfile(
                    profileId,
                    userId,
                    p.categories(),
                    null,
                    now,
                    now
            );

            if (p.channels() != null) {
                for (var ch : p.channels()) {
                    if (ch == null) continue;
                    influencerProfileCommandMapper.insertChannel(
                            UUID.randomUUID().toString(),
                            profileId,
                            ch.platform(),
                            ch.channelUrl(),
                            ch.channelHandle(),
                            ch.followers(),
                            now,
                            now
                    );
                }
            }

            influencerVerificationRepository.save(new InfluencerVerificationEntity(
                    UUID.randomUUID().toString(),
                    userId,
                    VerificationStatus.PENDING,
                    p.verification() == null ? null : p.verification().method(),
                    LocalDateTime.now(),
                    p.verification() == null ? null : p.verification().data()
            ));
        }

        return new AuthResult(user, null);
    }

    @Transactional
    public AuthResult login(String email, String password) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new DomainException("INVALID_ARGUMENT", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        UserEntity user = userOpt.get();
        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new DomainException("INVALID_ARGUMENT", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 인플루언서는 ADMIN 승인(verification VERIFIED + status ACTIVE) 전까지 로그인 불가
        if ("INFLUENCER".equalsIgnoreCase(user.getRole())) {
            if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                throw new DomainException("FORBIDDEN", "관리자 승인 후 로그인할 수 있습니다.");
            }
            var v = influencerVerificationRepository
                    .findFirstByUserIdOrderBySubmittedAtDesc(user.getId())
                    .orElse(null);
            if (v == null || v.getStatus() != VerificationStatus.VERIFIED) {
                throw new DomainException("FORBIDDEN", "관리자 승인 후 로그인할 수 있습니다.");
            }
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return new AuthResult(user, null);
    }

    @Transactional
    public TokenPair issueTokensAndCreateSession(UserEntity user, String userAgent, String ip) {
        TokenPair pair = tokenService.generateTokenPair(user.getId(), user.getEmail(), user.getRole());

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(14);
        String uaHash = hashNullable(userAgent);
        String ipHash = hashNullable(ip);

        RefreshSessionEntity session = new RefreshSessionEntity(
                UUID.randomUUID().toString(),
                user.getId(),
                pair.jti(),
                uaHash,
                ipHash,
                expiresAt
        );
        refreshSessionRepository.save(session);

        return pair;
    }

    @Transactional
    public TokenPair refresh(String refreshToken, String userAgent, String ip) {
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)))
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        String userId = claims.getSubject();
        String jti = claims.getId();
        if (userId == null || jti == null) {
            throw new DomainException("INVALID_ARGUMENT", "유효하지 않은 Refresh 토큰");
        }

        RefreshSessionEntity session = refreshSessionRepository.findByJti(jti)
                .orElseThrow(() -> new DomainException("INVALID_ARGUMENT", "유효하지 않은 Refresh 토큰"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new DomainException("INVALID_ARGUMENT", "만료된 Refresh 토큰");
        }

        // UA/IP 해시는 선택이라, 값이 저장된 경우에만 비교 (Nest와 유사)
        String uaHash = hashNullable(userAgent);
        String ipHash = hashNullable(ip);
        if (session.getUaHash() != null && uaHash != null && !session.getUaHash().equals(uaHash)) {
            throw new DomainException("INVALID_ARGUMENT", "유효하지 않은 Refresh 토큰");
        }
        if (session.getIpHash() != null && ipHash != null && !session.getIpHash().equals(ipHash)) {
            throw new DomainException("INVALID_ARGUMENT", "유효하지 않은 Refresh 토큰");
        }

        UserEntity user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new DomainException("INVALID_ARGUMENT", "User not found"));

        // rotate: 기존 session 삭제 후 새로 생성
        refreshSessionRepository.delete(session);
        return issueTokensAndCreateSession(user, userAgent, ip);
    }

    @Transactional
    public void logout(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)))
                    .requireIssuer(jwtProperties.issuer())
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();
            String jti = claims.getId();
            if (jti != null) {
                refreshSessionRepository.findByJti(jti).ifPresent(refreshSessionRepository::delete);
            }
        } catch (Exception ignored) {
        }
    }

    @Transactional
    public long logoutAll(String userId) {
        return refreshSessionRepository.deleteByUserId(userId);
    }

    public long activeSessionCount(String userId) {
        return refreshSessionRepository.countByUserId(userId);
    }

    private static String hashNullable(String input) {
        if (input == null || input.isBlank()) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            return null;
        }
    }

    public record AuthResult(UserEntity user, Object identities) {}
}

