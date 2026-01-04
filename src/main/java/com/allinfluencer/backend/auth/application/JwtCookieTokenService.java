package com.allinfluencer.backend.auth.application;

import com.allinfluencer.backend.common.security.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtCookieTokenService {
    private final JwtProperties props;

    public JwtCookieTokenService(JwtProperties props) {
        this.props = props;
    }

    public TokenPair generateTokenPair(String userId, String email, String role) {
        String jti = UUID.randomUUID().toString();

        Instant now = Instant.now();
        Instant accessExp = now.plusSeconds(props.accessTokenSeconds());
        Instant refreshExp = now.plus(Duration.ofDays(14));

        String accessToken = Jwts.builder()
                .issuer(props.issuer())
                .subject(userId)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(accessExp))
                .claims(Map.of(
                        "email", email,
                        "role", role
                ))
                .signWith(Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8)))
                .compact();

        String refreshToken = Jwts.builder()
                .issuer(props.issuer())
                .subject(userId)
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(refreshExp))
                .signWith(Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8)))
                .compact();

        return new TokenPair(accessToken, refreshToken, jti, (int) props.accessTokenSeconds());
    }

    public ResponseCookie buildAccessCookie(String accessToken) {
        return ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(props.accessTokenSeconds())
                .build();
    }

    public ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(14))
                .build();
    }

    public ResponseCookie clearAccessCookie() {
        return ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
    }

    public ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
    }

    public record TokenPair(String accessToken, String refreshToken, String jti, int expiresInSeconds) {}
}

