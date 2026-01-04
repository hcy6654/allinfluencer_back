package com.allinfluencer.backend.oauth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oauth")
public record OAuthProperties(
        String redirectSuccess,
        String redirectFailure
) {
}

