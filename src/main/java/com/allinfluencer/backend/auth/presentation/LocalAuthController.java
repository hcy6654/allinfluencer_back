package com.allinfluencer.backend.auth.presentation;

import com.allinfluencer.backend.auth.application.AuthApplicationService;
import com.allinfluencer.backend.auth.application.JwtCookieTokenService;
import com.allinfluencer.backend.mypage.domain.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * NestJS LocalAuthController 호환(최소):
 * - POST /auth/local/signup
 * - POST /auth/local/login
 */
@RestController
@RequestMapping("/auth/local")
public class LocalAuthController {
    private final AuthApplicationService authService;
    private final JwtCookieTokenService tokenService;

    public LocalAuthController(AuthApplicationService authService, JwtCookieTokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> signup(
            @Valid @RequestBody LocalSignupRequest body,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        // Local은 기본 INFLUENCER로 생성 (승인 필요)
        var result = authService.signup(body.email(), body.password(), body.displayName(), body.role(), body.influencer());

        // 인플루언서는 승인 전 로그인 불가 => 여기서는 토큰 발급하지 않음
        if ("INFLUENCER".equalsIgnoreCase(result.user().getRole())) {
            return Map.of(
                    "success", true,
                    "message", "회원가입이 완료되었습니다. 관리자 승인 후 로그인할 수 있습니다.",
                    "user", Map.of(
                            "id", result.user().getId(),
                            "email", result.user().getEmail(),
                            "displayName", result.user().getDisplayName(),
                            "role", result.user().getRole()
                    )
            );
        }

        var pair = authService.issueTokensAndCreateSession(result.user(), req.getHeader("user-agent"), req.getRemoteAddr());
        res.addHeader(HttpHeaders.SET_COOKIE, tokenService.buildAccessCookie(pair.accessToken()).toString());
        res.addHeader(HttpHeaders.SET_COOKIE, tokenService.buildRefreshCookie(pair.refreshToken()).toString());

        return Map.of(
                "success", true,
                "message", "회원가입이 완료되었습니다.",
                "user", Map.of(
                        "id", result.user().getId(),
                        "email", result.user().getEmail(),
                        "displayName", result.user().getDisplayName(),
                        "role", result.user().getRole()
                )
        );
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> login(
            @Valid @RequestBody LocalLoginRequest body,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        var result = authService.login(body.email(), body.password());
        var pair = authService.issueTokensAndCreateSession(result.user(), req.getHeader("user-agent"), req.getRemoteAddr());
        res.addHeader(HttpHeaders.SET_COOKIE, tokenService.buildAccessCookie(pair.accessToken()).toString());
        res.addHeader(HttpHeaders.SET_COOKIE, tokenService.buildRefreshCookie(pair.refreshToken()).toString());

        return Map.of(
                "success", true,
                "message", "로그인되었습니다.",
                "user", Map.of(
                        "id", result.user().getId(),
                        "email", result.user().getEmail(),
                        "displayName", result.user().getDisplayName(),
                        "role", result.user().getRole(),
                        "avatar", result.user().getAvatar()
                )
        );
    }

    public record LocalSignupRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 6) String password,
            String displayName,
            String role,
            // influencer signup payload를 그대로 받되, 최소 필드만 사용
            com.allinfluencer.backend.auth.presentation.dto.AuthDtos.InfluencerSignup influencer
    ) {}

    public record LocalLoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}
}

