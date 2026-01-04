package com.allinfluencer.backend.oauth.presentation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * NestJS OAuthController 호환
 * - GET /auth/google|kakao|naver : OAuth 시작(리다이렉트)
 * - GET /auth/{provider}/callback : (Spring Security가 처리)
 */
@RestController
@RequestMapping("/auth")
public class OAuthController {

    @GetMapping("/google")
    public void google(HttpServletResponse res) throws IOException {
        res.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/kakao")
    public void kakao(HttpServletResponse res) throws IOException {
        res.sendRedirect("/oauth2/authorization/kakao");
    }

    @GetMapping("/naver")
    public void naver(HttpServletResponse res) throws IOException {
        res.sendRedirect("/oauth2/authorization/naver");
    }

    // 콜백은 oauth2Login().redirectionEndpoint("/auth/*/callback") 에서 처리됨
}

