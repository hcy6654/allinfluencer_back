package com.allinfluencer.backend.mypage.presentation;

import com.allinfluencer.backend.common.security.AuthenticatedUser;
import com.allinfluencer.backend.mypage.domain.DomainException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * NestJS MyPageController 호환:
 * - GET /my  (역할별 리다이렉트 정보 반환)
 */
@RestController
@RequestMapping("/my")
public class MyPageController {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> redirectToRolePage(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null) throw new DomainException("INVALID_ARGUMENT", "Unauthorized");

        String role = user.role();
        if ("INFLUENCER".equals(role)) {
            return Map.of("url", "/my/influencer");
        }
        if ("ADVERTISER".equals(role)) {
            return Map.of("url", "/my/advertiser");
        }
        return Map.of("url", "/my/influencer");
    }
}

