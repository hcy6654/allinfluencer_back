package com.allinfluencer.backend.auth.presentation;

import com.allinfluencer.backend.auth.application.AuthApplicationService;
import com.allinfluencer.backend.auth.application.JwtCookieTokenService;
import com.allinfluencer.backend.auth.infrastructure.jpa.UserEntity;
import com.allinfluencer.backend.auth.infrastructure.jpa.UserRepository;
import com.allinfluencer.backend.signup.infrastructure.jpa.AdvertiserCompanyRepository;
import com.allinfluencer.backend.signup.infrastructure.jpa.AdvertiserVerificationRepository;
import com.allinfluencer.backend.signup.infrastructure.jpa.InfluencerVerificationRepository;
import com.allinfluencer.backend.common.security.AuthenticatedUser;
import com.allinfluencer.backend.mypage.domain.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.allinfluencer.backend.auth.presentation.dto.AuthDtos.LoginRequest;
import static com.allinfluencer.backend.auth.presentation.dto.AuthDtos.SignupRequest;

/**
 * NestJS AuthController 호환:
 * - POST /auth/signup
 * - POST /auth/login
 * - POST /auth/refresh
 * - POST /auth/logout
 * - POST /auth/logout-all
 * - GET  /auth/me
 * - GET  /auth/sessions
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthApplicationService authService;
    private final JwtCookieTokenService tokenService;
    private final UserRepository userRepository;
    private final AdvertiserCompanyRepository advertiserCompanyRepository;
    private final InfluencerVerificationRepository influencerVerificationRepository;
    private final AdvertiserVerificationRepository advertiserVerificationRepository;

    public AuthController(
            AuthApplicationService authService,
            JwtCookieTokenService tokenService,
            UserRepository userRepository,
            AdvertiserCompanyRepository advertiserCompanyRepository,
            InfluencerVerificationRepository influencerVerificationRepository,
            AdvertiserVerificationRepository advertiserVerificationRepository
    ) {
        this.authService = authService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.advertiserCompanyRepository = advertiserCompanyRepository;
        this.influencerVerificationRepository = influencerVerificationRepository;
        this.advertiserVerificationRepository = advertiserVerificationRepository;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> signup(
            @Valid @RequestBody SignupRequest body,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        Object rolePayload = null;
        String role = body.role() == null ? "INFLUENCER" : body.role().trim().toUpperCase();
        if ("INFLUENCER".equals(role)) {
            rolePayload = body.influencer();
        } else if ("ADVERTISER".equals(role)) {
            rolePayload = body.advertiser();
        }

        var result = authService.signup(body.email(), body.password(), body.displayName(), role, rolePayload);
        UserEntity user = result.user();

        // 인플루언서는 ADMIN 승인 전까지 "가입 완료(로그인)" 불가 => 토큰/쿠키 발급하지 않음
        if (!"INFLUENCER".equals(role)) {
            var pair = authService.issueTokensAndCreateSession(user, req.getHeader("user-agent"), req.getRemoteAddr());
            res.addHeader(HttpHeaders.SET_COOKIE, tokenService.buildAccessCookie(pair.accessToken()).toString());
            res.addHeader(HttpHeaders.SET_COOKIE, tokenService.buildRefreshCookie(pair.refreshToken()).toString());

            return Map.of(
                    "user", Map.of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "displayName", user.getDisplayName(),
                            "avatar", user.getAvatar(),
                            "role", user.getRole()
                    ),
                    "verification", Map.of(
                            "status", "PENDING",
                            "requiresApproval", false
                    ),
                    "accessToken", "",
                    "refreshToken", "",
                    "expiresIn", pair.expiresInSeconds()
            );
        }

        return Map.of(
                "user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "displayName", user.getDisplayName(),
                        "avatar", user.getAvatar(),
                        "role", user.getRole()
                ),
                "verification", Map.of(
                        "status", "PENDING",
                        "requiresApproval", true
                ),
                "message", "관리자 승인 후 로그인할 수 있습니다."
        );
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> login(
            @Valid @RequestBody LoginRequest body,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        var result = authService.login(body.email(), body.password());
        UserEntity user = result.user();

        var pair = authService.issueTokensAndCreateSession(user, req.getHeader("user-agent"), req.getRemoteAddr());
        res.addHeader(HttpHeaders.SET_COOKIE, tokenService.buildAccessCookie(pair.accessToken()).toString());
        res.addHeader(HttpHeaders.SET_COOKIE, tokenService.buildRefreshCookie(pair.refreshToken()).toString());

        return Map.of(
                "user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "displayName", user.getDisplayName(),
                        "avatar", user.getAvatar(),
                        "role", user.getRole()
                ),
                "accessToken", "",
                "refreshToken", "",
                "expiresIn", pair.expiresInSeconds()
        );
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> refresh(HttpServletRequest req, HttpServletResponse res) {
        String refreshToken = readCookie(req, "refresh_token");
        if (refreshToken == null) {
            throw new DomainException("INVALID_ARGUMENT", "Refresh token not found");
        }

        var pair = authService.refresh(refreshToken, req.getHeader("user-agent"), req.getRemoteAddr());
        res.addHeader(HttpHeaders.SET_COOKIE, tokenService.buildAccessCookie(pair.accessToken()).toString());
        res.addHeader(HttpHeaders.SET_COOKIE, tokenService.buildRefreshCookie(pair.refreshToken()).toString());

        return Map.of("success", true, "message", "Tokens refreshed successfully");
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> logout(HttpServletRequest req, HttpServletResponse res) {
        String refreshToken = readCookie(req, "refresh_token");
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        res.addHeader(HttpHeaders.SET_COOKIE, tokenService.clearAccessCookie().toString());
        res.addHeader(HttpHeaders.SET_COOKIE, tokenService.clearRefreshCookie().toString());
        return Map.of("success", true, "message", "Logged out successfully");
    }

    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> logoutAll(
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletResponse res
    ) {
        if (user == null) throw new DomainException("INVALID_ARGUMENT", "Unauthorized");
        long deleted = authService.logoutAll(user.userId());
        res.addHeader(HttpHeaders.SET_COOKIE, tokenService.clearAccessCookie().toString());
        res.addHeader(HttpHeaders.SET_COOKIE, tokenService.clearRefreshCookie().toString());
        return Map.of("success", true, "message", "Logged out from all devices successfully", "sessionCount", deleted);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> me(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null) throw new DomainException("INVALID_ARGUMENT", "Unauthorized");
        UserEntity full = userRepository.findById(user.userId()).orElse(null);
        var advertiserCompany = advertiserCompanyRepository.findByUserId(user.userId()).orElse(null);
        Map<String, Object> company = advertiserCompany == null
                ? null
                : Map.of(
                        "companyName", advertiserCompany.getCompanyName(),
                        "industry", advertiserCompany.getIndustry()
                );

        String role = user.role() == null ? "" : user.role().trim().toUpperCase();
        String verificationStatus = null;
        if ("INFLUENCER".equals(role)) {
            verificationStatus = influencerVerificationRepository
                    .findFirstByUserIdOrderBySubmittedAtDesc(user.userId())
                    .map(v -> v.getStatus().name())
                    .orElse(null);
        } else if ("ADVERTISER".equals(role)) {
            verificationStatus = advertiserVerificationRepository
                    .findFirstByUserIdOrderBySubmittedAtDesc(user.userId())
                    .map(v -> v.getStatus().name())
                    .orElse(null);
        }

        return Map.of(
                "success", true,
                "user", Map.of(
                        "id", user.userId(),
                        "email", full == null ? null : full.getEmail(),
                        "displayName", full == null ? null : full.getDisplayName(),
                        "avatar", full == null ? null : full.getAvatar(),
                        "role", role,
                        "status", full == null ? null : full.getStatus(),
                        "advertiserCompany", company
                ),
                "verification", Map.of(
                        "status", verificationStatus
                )
        );
    }

    @GetMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> sessions(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null) throw new DomainException("INVALID_ARGUMENT", "Unauthorized");
        long count = authService.activeSessionCount(user.userId());
        return Map.of("success", true, "activeSessionCount", count, "message", count + " active session(s) found");
    }

    private static String readCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        for (var c : req.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}

