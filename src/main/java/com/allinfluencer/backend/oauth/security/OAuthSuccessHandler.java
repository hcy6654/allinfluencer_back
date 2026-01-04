package com.allinfluencer.backend.oauth.security;

import com.allinfluencer.backend.auth.application.AuthApplicationService;
import com.allinfluencer.backend.auth.application.JwtCookieTokenService;
import com.allinfluencer.backend.mypage.domain.DomainException;
import com.allinfluencer.backend.oauth.application.OAuthIntegrationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {
    private final OAuthIntegrationService integrationService;
    private final AuthApplicationService authService;
    private final JwtCookieTokenService tokenService;
    private final OAuthProperties props;

    public OAuthSuccessHandler(
            OAuthIntegrationService integrationService,
            AuthApplicationService authService,
            JwtCookieTokenService tokenService,
            OAuthProperties props
    ) {
        this.integrationService = integrationService;
        this.authService = authService;
        this.tokenService = tokenService;
        this.props = props;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauth)) {
            response.sendRedirect(props.redirectFailure() + "?error=integration_failed");
            return;
        }

        String provider = oauth.getAuthorizedClientRegistrationId();
        String linkingUserId = readCookie(request, "linking_user_id");
        String action = linkingUserId != null ? "link" : "login";

        try {
            var profile = OAuthProfileExtractor.extract(provider, oauth.getPrincipal().getAttributes());
            var result = integrationService.integrate(provider, profile, linkingUserId);

            // linking 모드면 토큰 발급 없이 성공 페이지로
            if (linkingUserId != null) {
                clearLinkingCookies(response);
                response.sendRedirect(buildSuccessUrl(provider, "link", result.isNewUser(), result.isNewIdentity(), result.needsEmailVerification()));
                return;
            }

            // influencer는 승인 전 로그인 불가 (토큰 발급 금지)
            if ("INFLUENCER".equalsIgnoreCase(result.user().getRole())
                    && !"ACTIVE".equalsIgnoreCase(result.user().getStatus())) {
                response.sendRedirect(buildFailureUrl(provider, "needs_approval"));
                return;
            }

            var pair = authService.issueTokensAndCreateSession(result.user(), request.getHeader("user-agent"), request.getRemoteAddr());
            response.addHeader(HttpHeaders.SET_COOKIE, tokenService.buildAccessCookie(pair.accessToken()).toString());
            response.addHeader(HttpHeaders.SET_COOKIE, tokenService.buildRefreshCookie(pair.refreshToken()).toString());

            response.sendRedirect(buildSuccessUrl(provider, action, result.isNewUser(), result.isNewIdentity(), result.needsEmailVerification()));
        } catch (IllegalArgumentException e) {
            response.sendRedirect(buildFailureUrl(provider, e.getMessage()));
        } catch (DomainException e) {
            response.sendRedirect(buildFailureUrl(provider, "integration_failed"));
        } catch (Exception e) {
            response.sendRedirect(buildFailureUrl(provider, "integration_failed"));
        }
    }

    private String buildSuccessUrl(String provider, String action, boolean newUser, boolean newIdentity, boolean needsEmail) {
        return props.redirectSuccess()
                + "?provider=" + enc(provider)
                + "&action=" + enc(action)
                + "&new_user=" + newUser
                + "&new_identity=" + newIdentity
                + "&needs_email=" + needsEmail;
    }

    private String buildFailureUrl(String provider, String reason) {
        return props.redirectFailure()
                + "?provider=" + enc(provider)
                + "&error=" + enc(reason == null ? "integration_failed" : reason);
    }

    private static String readCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private static void clearLinkingCookies(HttpServletResponse res) {
        res.addHeader(HttpHeaders.SET_COOKIE, clearCookie("linking_user_id"));
    }

    private static String clearCookie(String name) {
        return org.springframework.http.ResponseCookie.from(name, "")
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build()
                .toString();
    }

    private static String enc(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }
}

