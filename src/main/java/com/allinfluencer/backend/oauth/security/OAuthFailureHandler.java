package com.allinfluencer.backend.oauth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuthFailureHandler implements AuthenticationFailureHandler {
    private final OAuthProperties props;

    public OAuthFailureHandler(OAuthProperties props) {
        this.props = props;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        String reason = "access_denied";
        String msg = exception == null ? null : exception.getMessage();
        if (msg != null && msg.toLowerCase().contains("access_denied")) {
            reason = "access_denied";
        }
        response.sendRedirect(props.redirectFailure() + "?error=" + enc(reason));
    }

    private static String enc(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }
}

