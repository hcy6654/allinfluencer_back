package com.allinfluencer.backend.auth.presentation.link;

import com.allinfluencer.backend.auth.infrastructure.jpa.UserIdentityRepository;
import com.allinfluencer.backend.auth.infrastructure.jpa.UserRepository;
import com.allinfluencer.backend.common.security.AuthenticatedUser;
import com.allinfluencer.backend.mypage.domain.DomainException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * NestJS AccountLinkController 호환(실동작):
 * - GET    /auth/link              : 연결된 계정 목록
 * - DELETE /auth/link/{provider}   : 계정 연결 해제
 * - GET    /auth/link/{provider}   : 계정 연결 시작(쿠키에 linking_user_id 저장 후 OAuth로 리다이렉트)
 */
@RestController
@RequestMapping("/auth/link")
@PreAuthorize("isAuthenticated()")
public class AccountLinkController {
    private final UserIdentityRepository identityRepository;
    private final UserRepository userRepository;

    public AccountLinkController(UserIdentityRepository identityRepository, UserRepository userRepository) {
        this.identityRepository = identityRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public Map<String, Object> linkedAccounts(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null) throw new DomainException("INVALID_ARGUMENT", "Unauthorized");

        var identities = identityRepository.findByUserIdOrderByLinkedAtDesc(user.userId());
        var userEntity = userRepository.findById(user.userId()).orElse(null);
        boolean hasPassword = userEntity != null && userEntity.getPasswordHash() != null;

        return Map.of(
                "identities", identities.stream().map(i -> Map.of(
                        "provider", i.getProvider().toLowerCase(),
                        "email", i.getEmail(),
                        "linkedAt", i.getLinkedAt(),
                        "lastUpdated", i.getLinkedAt()
                )).toList(),
                "hasPassword", hasPassword,
                "primaryEmail", userEntity == null ? null : userEntity.getEmail(),
                "totalAuthMethods", identities.size() + (hasPassword ? 1 : 0)
        );
    }

    @GetMapping("/{provider}")
    public void startLink(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String provider,
            HttpServletResponse res
    ) throws java.io.IOException {
        if (user == null) throw new DomainException("INVALID_ARGUMENT", "Unauthorized");
        String p = normalizeProvider(provider);

        res.addHeader(HttpHeaders.SET_COOKIE, org.springframework.http.ResponseCookie.from("linking_user_id", user.userId())
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(java.time.Duration.ofMinutes(10))
                .build().toString());

        res.sendRedirect("/oauth2/authorization/" + p);
    }

    @DeleteMapping("/{provider}")
    public Map<String, Object> unlink(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String provider) {
        if (user == null) throw new DomainException("INVALID_ARGUMENT", "Unauthorized");

        String p = normalizeProvider(provider).toUpperCase();
        var identities = identityRepository.findByUserIdOrderByLinkedAtDesc(user.userId());
        var userEntity = userRepository.findById(user.userId()).orElse(null);
        boolean hasPassword = userEntity != null && userEntity.getPasswordHash() != null;

        int totalAuthMethods = identities.size() + (hasPassword ? 1 : 0);
        if (totalAuthMethods <= 1) {
            throw new DomainException("FORBIDDEN", "Cannot unlink the last authentication method. Please set a password first.");
        }

        long deleted = identityRepository.deleteByUserIdAndProvider(user.userId(), p);
        if (deleted == 0) {
            throw new DomainException("INVALID_ARGUMENT", "No linked account found for this provider");
        }

        return Map.of("success", true, "message", provider + " account has been successfully unlinked");
    }

    private static String normalizeProvider(String provider) {
        if (provider == null) throw new DomainException("INVALID_ARGUMENT", "Invalid provider");
        String p = provider.trim().toLowerCase();
        List<String> valid = List.of("google", "kakao", "naver");
        if (!valid.contains(p)) throw new DomainException("INVALID_ARGUMENT", "Invalid provider");
        return p;
    }
}

