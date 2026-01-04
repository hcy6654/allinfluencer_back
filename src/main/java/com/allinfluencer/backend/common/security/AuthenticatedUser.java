package com.allinfluencer.backend.common.security;

public record AuthenticatedUser(
        String userId,
        String role
) {
}


